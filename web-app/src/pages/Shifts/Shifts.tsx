import {Accordion, AccordionDetails, AccordionSummary, Button, Paper, TextField} from "@mui/material";
import {FormEvent, useState} from "react";
import axios, {AxiosResponse} from "axios";
import {useAuthentication} from "../../contexts/AuthenticationContext/AuthenticationContext.tsx";
import {TokenAuthentication} from "../../contexts/AuthenticationContext/Authentication.tsx";
import {format, parseISO} from "date-fns";
import {da} from "date-fns/locale";
import {DayPilot, DayPilotCalendar} from "@daypilot/daypilot-lite-react";
import {brown, lightGreen, purple, red, yellow} from "@mui/material/colors";
import {WeekPlan} from "./WeekPlan.ts";
import {Shift} from "./Shift.ts";
import {ExpandMore} from "@mui/icons-material";
import CalendarBeforeCellRenderArgs = DayPilot.CalendarBeforeCellRenderArgs;

interface Helper {
    color: string
}
const helpers: {[name: string]: Helper} = {
    camilla: {
        color: lightGreen["A100"],
    },
    helle: {
        color: purple["100"],
    },
    jona: {
        color: yellow["100"],
    },
    tex: {
        color: brown["100"],
    }
}

interface HelperBookingDto {
    type: string,
    shortName: string,
}

interface ShiftDto {
    shiftId: string,
    start: string,
    end: string,
    helperBooking: HelperBookingDto,
}

interface WeekPlanDto {
    week: string,

    monday: ShiftDto[],
    tuesday: ShiftDto[],
    wednesday: ShiftDto[],
    thursday: ShiftDto[],
    friday: ShiftDto[],
    saturday: ShiftDto[],
    sunday: ShiftDto[],
}

export default function Shifts() {
    const [unbooked, setUnbooked] = useState("")
    const [weekPlans, setWeekPlans] = useState<WeekPlan[]>([])
    const {authentication} = useAuthentication()
    const client = axios.create({
        baseURL: "http://localhost:8080/",
    });

    const [startWeek, setStartWeek] = useState("")
    const [endWeek, setEndWeek] = useState("")

    const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
        event.preventDefault()

        const yearWeekInterval = `${startWeek}--${endWeek}`
        if (authentication instanceof TokenAuthentication) {
            console.log("We're authenticated")
        }
        client.get(`shifts/${yearWeekInterval}`)
            // client.get(`shifts/${yearWeekInterval}`, {
            //     headers: {
            //         Authorization: `Bearer ${authentication.token}`
            //     },
            // })
            .then((weekPlans: AxiosResponse<WeekPlanDto[], any>) => {
                const toShift = (shift: ShiftDto) => {
                    let helper
                    if (shift.helperBooking.type === "Booking") {
                        helper = shift.helperBooking.shortName
                    } else {
                        helper = "Ikke booket"
                    }
                    return Shift.create({
                        shiftId: shift.shiftId,
                        start: parseISO(shift.start),
                        end: parseISO(shift.end),
                        helper: helper
                    })
                }
                return weekPlans.data.map((weekPlan) => {
                    return WeekPlan.create({
                        week: weekPlan.week,

                        monday: weekPlan.monday.map(toShift),
                        tuesday: weekPlan.tuesday.map(toShift),
                        wednesday: weekPlan.wednesday.map(toShift),
                        thursday: weekPlan.thursday.map(toShift),
                        friday: weekPlan.friday.map(toShift),
                        saturday: weekPlan.saturday.map(toShift),
                        sunday: weekPlan.sunday.map(toShift),
                    })
                })
            })
            .then((weekPlans: WeekPlan[]) => {
                const shifts = weekPlans.flatMap(weekPlan => {
                    return weekPlan.allShifts()
                })


                const shiftStr = shifts
                    .filter(shift => shift.helper === "Ikke booket")
                    .map(shift => {
                        const formattedDate = format(shift.start, "EEEE 'den' d. MMM:", {
                            locale: da
                        })

                        const formattedStart = format(shift.start, "HH:mm")
                        const formattedEnd = format(shift.end, "HH:mm")
                        const formattedInterval = `${formattedStart} - ${formattedEnd}`

                        return `${formattedDate}\n${formattedInterval}`
                    }).join("\n\n")

                setUnbooked(shiftStr)

                setWeekPlans(weekPlans)
            })
        // }
    }

    interface WeekPlanViewOptions {
        weekPlan: WeekPlan
    }

    const WeekPlanView = ({weekPlan}: WeekPlanViewOptions) => {
        const startDate = parseISO(`${weekPlan.week}-1`)

        const events: DayPilot.EventData[] = weekPlan.allShifts()
            .filter(shift => shift.helper !== "camille")
            .map(shift => {
                return {
                    id: shift.shiftId,
                    text: shift.helper,
                    start: new DayPilot.Date(shift.start, true),
                    end: new DayPilot.Date(shift.end, true),
                    backColor: helpers[shift.helper].color || red["100"],
                }
            })

        const cellHandler = (event: CalendarBeforeCellRenderArgs) => {
            if (event.cell.start.getHours() === 11) {
                event.cell.properties.business = false
            }
        }

        return (
            <DayPilotCalendar
                onBeforeCellRender={cellHandler}
                cellHeight={20}

                heightSpec="Full"
                events={events}
                headerDateFormat="dddd"
                durationBarVisible={false}
                viewType="Week"
                weekStarts={1}
                timeRangeSelectedHandling="Disabled"
                startDate={new DayPilot.Date(startDate, true)}
                timeFormat="Clock24Hours"/>
        )
    }

    return (
        // <AuthorizationContext>

        <div>
            <h1>Shifts</h1>

            {weekPlans.map((weekPlan, index) => (
                <Paper key={index}>
                    <h2>{weekPlan.week}</h2>
                    <WeekPlanView weekPlan={weekPlan}/>
                </Paper>
            ))}

            <Accordion>
                <AccordionSummary
                    id="quick-message"
                    aria-controls="accordion-text-message"
                    expandIcon={<ExpandMore />} >
                    Text message
                </AccordionSummary>
                <AccordionDetails>
                    <pre>
                        {unbooked}
                    </pre>
                </AccordionDetails>
            </Accordion>

            <Accordion>
                <AccordionSummary
                    id="filters"
                    aria-controls="accordion-filters"
                    expandIcon={<ExpandMore />} >
                    Filters
                </AccordionSummary>
                <AccordionDetails>
                    Foobar
                </AccordionDetails>
            </Accordion>

            <form autoComplete="off" onSubmit={handleSubmit}>
                <div>
                    <TextField
                        label="From"
                        onChange={(e) => setStartWeek(e.target.value)}
                        required={true}
                        variant="outlined"
                        color="secondary"
                        type="week"
                        value={startWeek}
                    />
                </div>

                <div>
                    <TextField
                        label="To"
                        onChange={(e) => setEndWeek(e.target.value)}
                        required={true}
                        variant="outlined"
                        color="secondary"
                        type="week"
                        value={endWeek}
                    />
                </div>

                <Button
                    variant="outlined"
                    color="secondary"
                    type="submit">
                    Read
                </Button>
            </form>
        </div>
        // </AuthorizationContext>
    )
}
