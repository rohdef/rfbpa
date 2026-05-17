import {Accordion, AccordionDetails, AccordionSummary, Checkbox, FormControlLabel, FormGroup} from "@mui/material";
import React, {useMemo, useState} from "react";
import {ExpandMore} from "@mui/icons-material";
import ShiftShedule from "./ShiftShedule.tsx"
import {HelperStorage} from "../../helpers/HelperStorage.ts"
import {DayPilot, DayPilotNavigator} from "@daypilot/daypilot-lite-react"
import {RfbpaClientProvider} from "../../contexts/UserProfileContext/RfbpaClientContext.tsx"
import Date = DayPilot.Date

export default function Shifts() {
    const [startDate, setStartDate] = useState(Date.today)

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

    const helpers = useMemo(() => new HelperStorage(), [])

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
                        <ShiftShedule
                            date={startDate}
                            helpers={helpers}
                        />
                    </div>
                </div>
            </div>
        </RfbpaClientProvider>
    )
}