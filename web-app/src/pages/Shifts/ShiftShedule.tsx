import {DayPilot, DayPilotCalendar} from "@daypilot/daypilot-lite-react"
import {brown, lightGreen, pink, purple, red, yellow} from "@mui/material/colors"
import {Helper} from "../../helpers/Helper.ts"
import {RfbpaClientProvider, useRfbpaClient} from "../../contexts/UserProfileContext/RfbpaClientContext.tsx"
import {useEffect, useState} from "react"
import {Alert, CircularProgress} from "@mui/material"
import MenuItemClickArgs = DayPilot.MenuItemClickArgs
import CalendarTimeRangeSelectedArgs = DayPilot.CalendarTimeRangeSelectedArgs


interface HelperVisual {
    color: string
}

interface VisualSettings {
    helpers: {
        [shortName: string]: HelperVisual,
        default: HelperVisual,
    }
}

const visualSettings: VisualSettings = {
    helpers: {
        camilla: {
            color: lightGreen["A100"],
        },
        helle: {
            color: purple["100"],
        },
        stella: {
            color: pink["200"],
        },
        tex: {
            color: brown["100"],
        },
        ulrik: {
            color: pink["100"],
        },
        walter: {
            color: yellow["100"],
        },
        default: {
            color: red["100"],
        },
    },
}



const deleteShift = async (shiftId: string) => {
    console.log(`Deleting shift with ID: ${shiftId}`)
}

const createShift = async (start: DayPilot.Date, end: DayPilot.Date) => {
    console.log(`Creating shift from ${start} to ${end}`)
}

const timeRangeSelect = async (args: CalendarTimeRangeSelectedArgs) => {
    const calendar = args.control
    calendar.clearSelection();
    createShift(args.start, args.end)
}

interface ShiftSheduleProps {
    date: DayPilot.Date,
}

enum CalendarState {
    LOADING,
    ERROR,
    READY,
}

const dateToWeek = (date: DayPilot.Date) => {
    const year = date.getYear()
    const weekNumber = `${date.weekNumberISO()}`.padStart(2, '0')
    return `${year}-W${weekNumber}`
}

export default function ShiftShedule({date}: ShiftSheduleProps) {
    const {rfbpaClient} = useRfbpaClient()
    const [calendarState, setCalendarState] = useState<CalendarState>(CalendarState.LOADING)
    const [events, setEvents] = useState<DayPilot.EventData[]>([])
    const [helpers, setHelpers] = useState<Helper[]>([])

    useEffect(() => {
        rfbpaClient.getHelpers()
            .then(setHelpers)
    }, []);

    useEffect(() => {
        setCalendarState(CalendarState.LOADING)

        const week = dateToWeek(date)
        rfbpaClient.getShiftsInWeek(week)
            .then(it => it.allShifts() )
            .then(shifts => {
                return shifts
                    .map(shift => {
                        return {shift, helper: helpers.find(it => it.id === shift.helper)}
                    })
                    .map(({shift, helper}) => {
                        if (helper) {
                            return {shift, helper}
                        } else {
                            const unknownHelper = Helper.create({
                                id: shift.helper,
                                name: "Ikke booket",
                            })
                            return { shift, helper: unknownHelper }
                        }
                    })
                    // .filter((shiftHelper) => !shiftHelper.helper.filtered)
                    .map(({shift, helper}): DayPilot.EventData => {
                        return {
                            id: shift.shiftId,
                            text: helper.name,
                            start: new DayPilot.Date(shift.start, true),
                            end: new DayPilot.Date(shift.end, true),
                            backColor: visualSettings.helpers[helper.id]?.color ?? visualSettings.helpers.default.color,
                        }
                    })
            })
            .then(events => {
                setEvents(events)
                setCalendarState(CalendarState.READY)
            })
            .catch(error => {
                setCalendarState(CalendarState.ERROR)
                setEvents([])
                console.error("Error loading shifts:", error)
            })
    }, [date, helpers, rfbpaClient])

    const registerIllness = async (shiftEvent: DayPilot.EventData) => {
        const newShiftId = await rfbpaClient.registerIllness(shiftEvent.id.toString())

        const newEvents = events.filter(e => e.id !== shiftEvent.id)
        const newEvent: DayPilot.EventData = {
            id: newShiftId.shiftId,
            text: "Ikke booket",
            start: shiftEvent.start,
            end: shiftEvent.end,
            backColor: red["100"],
        }
        newEvents.push(newEvent)
        setEvents(newEvents)
    }

    const changeBooking = async (shiftId: string, helperId: string) => {
        console.log(shiftId, helperId)
    }

    const helperMenuItems = helpers.map(helper => ({
        text: helper.name,
        onClick: (args: MenuItemClickArgs) => {
            changeBooking(args.source.data.id, helper.id)
        }
    }))

    const contextMenu = new DayPilot.Menu({
        items: [
            ...helperMenuItems,
            {
                text: "-"
            },
            {
                text: "Register illness",
                onClick: (args: MenuItemClickArgs) => { registerIllness(args.source.data) }
            },
            {
                text: "-"
            },
            {
                text: "Delete",
                onClick: (args: MenuItemClickArgs) => { deleteShift(args.source.data.id) }
            },
        ]
    })

    const beforeRender = (args: DayPilot.CalendarBeforeEventRenderArgs) => {
        args.data.areas = [
            {
                top: 3,
                right: 3,
                width: 20,
                height: 20,
                symbol: "icons/daypilot.svg#threedots-v",
                fontColor: "#000",
                toolTip: "Show context menu",
                action: "ContextMenu",
            },
            {
                top: 3,
                right: 25,
                width: 20,
                height: 20,
                symbol: "icons/daypilot.svg#x-2",
                fontColor: "#000",
                action: "None",
                toolTip: "Delete event",
                onClick: async args => {
                    deleteShift(args.source.data.id)
                }
            }
        ]
    }

    return (
        <>
            {calendarState == CalendarState.ERROR ?
                    (<Alert severity="error">"Halløj"</Alert>) : null
            }
            {calendarState == CalendarState.LOADING ?
                    (<CircularProgress />) : null
            }

            <RfbpaClientProvider>
                <DayPilotCalendar
                    events={events}
                    headerTextWrappingEnabled={false}

                    cellHeight={15}
                    cellDuration={15}
                    headerDateFormat="dddd"
                    viewType="Week"
                    durationBarVisible={false}
                    timeRangeSelectedHandling="Enabled"
                    weekStarts={1}
                    startDate={date}
                    timeFormat="Clock24Hours"

                    contextMenu={contextMenu}

                    onBeforeEventRender={beforeRender}
                    onTimeRangeSelected={timeRangeSelect}
                />
            </RfbpaClientProvider>
        </>
    )
}