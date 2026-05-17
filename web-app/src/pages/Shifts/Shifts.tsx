import {
    Accordion,
    AccordionDetails,
    AccordionSummary, Alert, CircularProgress,
    Checkbox,
    FormControlLabel,
    FormGroup
} from "@mui/material";
import React, {useEffect, useState} from "react";
import {WeekPlan} from "./WeekPlan.ts";
import {ExpandMore} from "@mui/icons-material";
import ShiftShedule from "./ShiftShedule.tsx"
import {HelperStorage} from "../../helpers/HelperStorage.ts"
import {DayPilot, DayPilotNavigator} from "@daypilot/daypilot-lite-react"
import {RfbpaClientProvider, useRfbpaClient} from "../../contexts/UserProfileContext/RfbpaClientContext.tsx"
import Date = DayPilot.Date

const dateToWeek = (date: Date) => {
    const year = date.getYear()
    const weekNumber = `${date.weekNumberISO()}`.padStart(2, '0')
    return `${year}-W${weekNumber}`
}

export default function Shifts() {
    const {rfbpaClient} = useRfbpaClient()
    const emptyPlan = WeekPlan.create({
        week: dateToWeek(Date.today()),

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
    const [isLoading, setIsLoading] = useState(false)
    const [error, setError] = useState<string | null>(null)

    useEffect(() => {
        const week = dateToWeek(startDate)

        setIsLoading(true)
        setError(null)
        rfbpaClient.getShiftsInWeek(week)
            .then(weekPlan => {
                setError(null)
                setWeekPlan(weekPlan)
            })
            .catch(e => {
                console.error('Failed to fetch shifts', e)
                setError('Failed to fetch shifts. Please try again later.')
            })
            .finally(() => setIsLoading(false))
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
        <RfbpaClientProvider>
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
                        {isLoading && <CircularProgress />}
                        {error && <Alert severity="error">{error}</Alert>}
                        {!isLoading && !error && (
                            <ShiftShedule
                                weekPlan={weekPlan}
                                helpers={helpers}
                            />
                        )}
                    </div>
                </div>
            </div>
        </RfbpaClientProvider>
    )
}