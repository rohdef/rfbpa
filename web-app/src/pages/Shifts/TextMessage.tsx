import React, {FormEvent, useState} from "react"
import {WeekPlan} from "./WeekPlan.ts"
import {Accordion, AccordionDetails, AccordionSummary, Box, Button, Grid, TextField} from "@mui/material"
import {ExpandMore} from "@mui/icons-material"
import {useAuthentication} from "../../contexts/AuthenticationContext/AuthenticationContext.tsx"
import axios, {AxiosResponse} from "axios"
import {TokenAuthentication} from "../../contexts/AuthenticationContext/Authentication.tsx"
import {Shift} from "./Shift.ts"
import {format, parseISO} from "date-fns"
import {da} from "date-fns/locale"

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

export default function TextMessage() {
    const {authentication} = useAuthentication()
    const client = axios.create({
        baseURL: "http://localhost:8080/api/public",
    });

    const [unbooked, setUnbooked] = useState("")
    const [weekPlans, setWeekPlans] = useState<WeekPlan[]>([])
    const [startWeek, setStartWeek] = useState("")
    const [endWeek, setEndWeek] = useState("")

    const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
        event.preventDefault()

        const yearWeekInterval = `${startWeek}--${endWeek}`
        if (authentication instanceof TokenAuthentication) {
            client.get(`shifts/in-interval/${yearWeekInterval}`, {
                headers: {
                    Authorization: `Bearer ${authentication.token}`
                },
            })
                .then((weekPlans: AxiosResponse<WeekPlanDto[], any>) => {
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
        }
    }

    return (
        <>
            <Accordion sx={{ marginBottom: 2 }}>
                <AccordionSummary
                    id="quick-message"
                    aria-controls="accordion-text-message"
                    expandIcon={<ExpandMore/>}>
                    Text message
                </AccordionSummary>
                <AccordionDetails>
                    <pre>
                        {unbooked}
                    </pre>
                </AccordionDetails>
            </Accordion>

            <Box component="form" autoComplete="off" onSubmit={handleSubmit}>
                <Grid container spacing={2}>
                    <Grid item xs={12} sm={6}>
                        <TextField
                            label="From"
                            margin="normal"
                            fullWidth
                            required
                            onChange={(e) => setStartWeek(e.target.value)}
                            variant="outlined"
                            color="secondary"
                            type="week"
                            value={startWeek}
                        />
                    </Grid>

                    <Grid item xs={12} sm={6}>
                        <TextField
                            label="To"
                            margin="normal"
                            fullWidth
                            required
                            onChange={(e) => setEndWeek(e.target.value)}
                            variant="outlined"
                            color="secondary"
                            type="week"
                            value={endWeek}
                        />
                    </Grid>
                </Grid>

                <Button
                    fullWidth
                    variant="contained"
                    color="secondary"
                    type="submit"
                    sx={{ marginTop: 2, marginBottom: 2 }}>
                    Read
                </Button>
            </Box>
        </>
    )
}