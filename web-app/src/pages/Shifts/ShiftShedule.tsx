import {DayPilot, DayPilotCalendar} from "@daypilot/daypilot-lite-react"
import {brown, grey, lightGreen, pink, purple, red, yellow} from "@mui/material/colors"
import {RfbpaClientProvider, useRfbpaClient} from "../../contexts/UserProfileContext/RfbpaClientContext.tsx"
import {useEffect, useState} from "react"
import {Alert, CircularProgress} from "@mui/material"
import {Shift} from "./Shift.ts"
import {HelperBooking} from "./Booking.ts"
import {useHelpers} from "../../contexts/HelpersContext/HelpersContext.tsx"
import MenuItemClickArgs = DayPilot.MenuItemClickArgs
import CalendarTimeRangeSelectedArgs = DayPilot.CalendarTimeRangeSelectedArgs


interface HelperVisual {
    color: string
}

interface VisualSettings {
    helpers: {
        [shortName: string]: HelperVisual,
        default: HelperVisual,
        noHelper: HelperVisual,
    }
}

const visualSettings: VisualSettings = {
    helpers: {
        "cb295b4b-98d9-4ef6-9e21-8e72eced814a": {
            // Camilla
            color: lightGreen["A100"],
        },
        "3f42d1ec-96ab-4554-bd78-eeb5406e8970": {
            // Helle
            color: purple["100"],
        },
        "93b2edf4-e6ff-4a9d-bf55-c7cf7eac1e86": {
            // Janus
            color: red["100"],
        },
        "c6fff973-5f84-4e8c-964e-caa932edcd3c": {
            // Stella
            color: pink["200"],
        },
        "d4ce9adf-f00b-4f76-b883-eec91d30c344": {
            // Tex
            color: brown["100"],
        },
        "be85f9b3-dab5-4e30-a64f-2b53abd40a01": {
            // Ulrik
            color: pink["100"],
        },
        "cebceeac-9f8f-4874-95d5-63caab4fe61e": {
            // Walter
            color: yellow["100"],
        },
        default: {
            color: red["100"],
        },
        noHelper: {
            color: grey["50"],
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
    const {helpers} = useHelpers()
    const [calendarState, setCalendarState] = useState<CalendarState>(CalendarState.LOADING)
    const [events, setEvents] = useState<DayPilot.EventData[]>([])
    const [shifts, setShifts] = useState<Shift[]>([])

    useEffect(() => {
        const week = dateToWeek(date)
        rfbpaClient.getShiftsInWeek(week)
            .then(it => it.allShifts() )
            .then(setShifts)
            .catch(error => {
                setCalendarState(CalendarState.ERROR)
                setShifts([])
                console.error("Error loading shifts:", error)
            })
    }, [date]);

    useEffect(() => {
        setCalendarState(CalendarState.LOADING)

        const events = shifts
            // .filter((shiftHelper) => !shiftHelper.helper.filtered)
            .map((shift): DayPilot.EventData => {
                const booking = shift.booking

                if (booking instanceof HelperBooking) {
                    const helper = helpers.find(it => it.id === booking.id)

                    if (helper) {
                        return {
                            id: shift.shiftId,
                            text: helper.name,
                            start: new DayPilot.Date(shift.start, true),
                            end: new DayPilot.Date(shift.end, true),
                            backColor: visualSettings.helpers[helper.id]?.color ?? visualSettings.helpers.default.color,
                        }
                    } else {
                        return {
                            id: shift.shiftId,
                            text: "Ukendt hjælper",
                            start: new DayPilot.Date(shift.start, true),
                            end: new DayPilot.Date(shift.end, true),
                            backColor: visualSettings.helpers.default.color,
                        }
                    }
                } else {
                    return {
                        id: shift.shiftId,
                        text: "Ikke booket",
                        start: new DayPilot.Date(shift.start, true),
                        end: new DayPilot.Date(shift.end, true),
                        backColor: visualSettings.helpers.noHelper.color,
                    }
                }
            })
        setEvents(events)
        setCalendarState(CalendarState.READY)
    }, [helpers, shifts])

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
        const shift = shifts.find(it => it.shiftId === shiftId)!!
        const originalShiftList = shifts
        const newShift = shift.copy({
            booking: HelperBooking.create({id: helperId})
        })
        const newShiftList = shifts
            .map(it => it.shiftId === shiftId ? newShift : it)
        setShifts(newShiftList)

        await rfbpaClient.changeBooking(shiftId, helperId)
            .catch(error => {
                console.error("Error changeBooking:", error)
                setShifts(originalShiftList)
            })
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
            <RfbpaClientProvider>
                {calendarState == CalendarState.ERROR ?
                        (<Alert severity="error">Fejl i datalæsning"</Alert>) : null
                }
                {calendarState == CalendarState.LOADING ?
                        (<CircularProgress />) : null
                }

                <DayPilotCalendar
                    events={events}
                    headerTextWrappingEnabled={false}

                    businessBeginsHour={6}
                    businessEndsHour={23}
                    heightSpec="Full"
                    // cellHeight={15}
                    cellDuration={30}
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
