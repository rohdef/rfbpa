import {Accordion, AccordionDetails, AccordionSummary, Checkbox, FormControlLabel, FormGroup} from "@mui/material";
import React, {useEffect, useState} from "react";
import axios, {AxiosResponse} from "axios";
import {useAuthentication} from "../../contexts/AuthenticationContext/AuthenticationContext.tsx";
import {TokenAuthentication} from "../../contexts/AuthenticationContext/Authentication.tsx";
import {parseISO} from "date-fns";
import {WeekPlan} from "./WeekPlan.ts";
import {Shift} from "./Shift.ts";
import {ExpandMore} from "@mui/icons-material";
import AuthorizationContext from "../../contexts/AuthorizationContext/AuthorizationContext.tsx";
import ShiftShedule from "./ShiftShedule.tsx"
import {HelperStorage} from "../../helpers/HelperStorage.ts"
import {DayPilot, DayPilotNavigator} from "@daypilot/daypilot-lite-react"
import Date = DayPilot.Date

interface HelperBookingDto {
    type: string,
    name: string,
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
    const {authentication} = useAuthentication()
    const client = axios.create({
        baseURL: "http://localhost:8080/api/public",
    });

    const emptyPlan = WeekPlan.create({
        week: "2026-W01",

        monday: [],
        tuesday: [],
        wednesday: [],
        thursday: [],
        friday: [],
        saturday: [],
        sunday: [],
    })
    const [weekPlan, setWeekPlan] = useState(emptyPlan)
    const [startDate, setStartDate] = useState(Date.today)

    useEffect(() => {
        const year = startDate.getYear()
        const weekNumber = `${startDate.weekNumberISO()}`.padStart(2, '0')
        const week = `${year}-W${weekNumber}`
        const yearWeekInterval = `${week}--${week}`

        const toShift = (shift: ShiftDto) => {
            let helper
            if (shift.helperBooking.type === "Booking") {
                helper = shift.helperBooking.name
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

        if (authentication instanceof TokenAuthentication) {
            client.get(`shifts/in-interval/${yearWeekInterval}`, {
                headers: {
                    Authorization: `Bearer ${authentication.token}`
                },
            })
                .then((weekPlans: AxiosResponse<WeekPlanDto[], any>) => {
                    return weekPlans.data[0]
                })
                .then(weekPlan => {
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
                .then(setWeekPlan)
        }
    }, [startDate]);

    const styles = {
        wrap: {
            display: "flex"
        },
        left: {
            marginRight: "10px"
        },
        main: {
            flexGrow: "1"
        }
    }

    const helpers = new HelperStorage()

    return (
        <AuthorizationContext>
            <div>
                <h1>Shifts</h1>

                <div style={ styles.wrap }>
                    <div style={styles.left}>
                        <DayPilotNavigator
                            selectMode="Week"
                            showMonths={2}
                            weekStarts={1}
                            showWeekNumbers={true}
                            selectionDay={startDate}
                            onTimeRangeSelected={ args => { setStartDate(args.start) }}
                        />
                        <Accordion sx={{ marginBottom: 2 }}>
                            <AccordionSummary
                                id="filters"
                                aria-controls="accordion-filters"
                                expandIcon={<ExpandMore/>}>
                                Filters
                            </AccordionSummary>
                            <AccordionDetails>
                                <FormGroup>
                                    {helpers.all.map((helper) => (
                                        <FormControlLabel
                                            key={helper.id}
                                            label={helper.name}
                                            control={<Checkbox checked={helper.filtered} />}
                                            sx={{ backgroundColor: helper.color }}>
                                        </FormControlLabel>
                                    ))}
                                </FormGroup>
                            </AccordionDetails>
                        </Accordion>
                    </div>

                    <div style={ styles.main }>
                        <ShiftShedule
                            weekPlan={weekPlan}
                            helpers={helpers}
                        />
                    </div>
                </div>
            </div>
        </AuthorizationContext>
    )
}
