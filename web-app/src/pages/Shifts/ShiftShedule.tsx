import {DayPilot, DayPilotCalendar} from "@daypilot/daypilot-lite-react"
import {red} from "@mui/material/colors"
import {WeekPlan} from "./WeekPlan.ts"
import {parseISO} from "date-fns"
import {Helper} from "../../helpers/Helper.ts"
import {HelperStorage} from "../../helpers/HelperStorage.ts"
import MenuItemClickArgs = DayPilot.MenuItemClickArgs
import CalendarTimeRangeSelectedArgs = DayPilot.CalendarTimeRangeSelectedArgs
import {RfbpaClientProvider, useRfbpaClient} from "../../contexts/UserProfileContext/RfbpaClientContext.tsx"

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
    weekPlan: WeekPlan,
    helpers: HelperStorage,
}

export default function ShiftShedule({weekPlan, helpers}: ShiftSheduleProps) {
    const {rfbpaClient} = useRfbpaClient()

    const startDate = parseISO(`${weekPlan.week}-1`)
    const events: DayPilot.EventData[] = weekPlan.allShifts()
        .map(shift => {
            return {shift, helper: helpers.helper(shift.helper)}
        })
        .map(({shift, helper}) => {
            if (helper) {
                return {shift, helper}
            } else {
                const unknownHelper = Helper.create({
                    id: shift.helper,
                    name: "Ikke booket",
                    color: red["100"],
                    filtered: false,
                })
                return { shift, helper: unknownHelper }
            }
        })
        .filter((shiftHelper) => !shiftHelper.helper.filtered)
        .map(({shift, helper}) => {
            return {
                id: shift.shiftId,
                text: helper.name,
                start: new DayPilot.Date(shift.start, true),
                end: new DayPilot.Date(shift.end, true),
                backColor: helper.color,
            }
        })

    const registerIllness = async (shiftId: string) => {
        console.log(`Registering illness for shift with ID: ${shiftId}`)
        const newShiftId = await rfbpaClient.reportIllness(shiftId)
        console.log(`Shift ${shiftId} reported as ill, new shift ID: ${newShiftId}`)
    }

    const contextMenu = new DayPilot.Menu({
        items: [
            {
                text: "Register illness",
                onClick: (args: MenuItemClickArgs) => { registerIllness(args.source.data.id) }
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
                <DayPilotCalendar
                    events={events}

                    cellHeight={15}
                    cellDuration={15}
                    headerDateFormat="dddd"
                    viewType="Week"
                    durationBarVisible={false}
                    timeRangeSelectedHandling="Enabled"
                    weekStarts={1}
                    startDate={new DayPilot.Date(startDate, true)}
                    timeFormat="Clock24Hours"

                    contextMenu={contextMenu}

                    onBeforeEventRender={beforeRender}
                    onTimeRangeSelected={timeRangeSelect}
                />
            </RfbpaClientProvider>
        </>
    )
}