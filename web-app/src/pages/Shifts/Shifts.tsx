import AuthorizationContext from "../../contexts/AuthorizationContext/AuthorizationContext.tsx";
import {Button, TextField} from "@mui/material";
import {FormEvent, useState} from "react";
import axios from "axios";
import configuration from "../../configuration.ts";
import {useAuthentication} from "../../contexts/AuthenticationContext/AuthenticationContext.tsx";
import {TokenAuthentication} from "../../contexts/AuthenticationContext/Authentication.tsx";

export default function Shifts() {
    const {authentication} = useAuthentication()
    const client = axios.create({
        baseURL: configuration.apiUrl,
    });

    const [startWeek, setStartWeek] = useState("")
    const [endWeek, setEndWeek] = useState("")

    const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
        event.preventDefault()

        console.log(startWeek)
        console.log(endWeek)

        if (authentication instanceof TokenAuthentication) {
            client.get(`shifts`, {
                headers: {
                    Authorization: `Bearer ${authentication.token}`
                }
            })
                .then((shifts) => console.log(shifts.data))
        }
    }

    return (
        <AuthorizationContext>
            <div>
                <h1>Shifts</h1>

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
        </AuthorizationContext>
    )
}
