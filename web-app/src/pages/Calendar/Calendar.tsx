import {useEffect, useState} from "react";
import axios from "axios";
import {TokenAuthentication, useAuthentication} from "../../contexts/AuthenticationContext/AuthenticationContext.tsx";
import AuthorizationContext from "../../contexts/AuthorizationContext/AuthorizationContext.tsx";
import configuration from "../../configuration.ts";

export default function Calendar() {
    const {authentication} = useAuthentication()
    const client = axios.create({
        baseURL: configuration.apiUrl,
    });

    const [calendar, setCalendar] = useState<string>()

    useEffect(() => {
        if (authentication instanceof TokenAuthentication) {
            client.get("/calendar", {
                headers: {
                    Authorization: `Bearer ${authentication.token}`
                }
            })
                .then((calendar) => setCalendar(calendar.data))
        }
    }, [authentication])

    return (
        <AuthorizationContext>
            <div>
                <h1>Your personal calendar URL</h1>
                <p>
                    {calendar ? calendar : "Still loading url"}
                </p>
            </div>
        </AuthorizationContext>
    )
}
