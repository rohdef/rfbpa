import React, {ReactNode, useEffect, useState} from "react";
import {RfbpaClient} from "../../api/RfbpaClient.ts"
import AuthorizationContext from "../AuthorizationContext/AuthorizationContext.tsx"
import {useAuthentication} from "../AuthenticationContext/AuthenticationContext.tsx"
import {WeekPlan} from "../../pages/Shifts/WeekPlan.ts"
import {Authentication, NoAuthentication, TokenAuthentication} from "../AuthenticationContext/Authentication.tsx"

interface RfbpaClientValues {
    rfbpaClient: AuthenticatedRfbpaClient
}

class AuthenticatedRfbpaClient {
    private _authentication: Authentication
    private client: RfbpaClient

    constructor(client: RfbpaClient) {
        this.client = client
        this._authentication = NoAuthentication.instance()
    }

    set authentication(value: Authentication) {
        this._authentication = value
    }

    async getShiftsInWeek(week: string): Promise<WeekPlan> {
        if (this._authentication instanceof TokenAuthentication) {
            return await this.client.getShiftsInWeek(week, this._authentication)
        } else {
            throw new Error(`Cannot read week ${week} without authentication`)
        }
    }

    async reportIllness(shiftId: string): Promise<string> {
        if (this._authentication instanceof TokenAuthentication) {
            return await this.client.reportIllness(shiftId, this._authentication)
        } else {
            throw new Error(`Cannot report illness for shift ${shiftId} without authentication`)
        }
    }
}

const rawClient = new RfbpaClient("http://localhost:8080/api/public")
const rfbpaClient = new AuthenticatedRfbpaClient(rawClient)

export const RfbpaClientContext = React.createContext<RfbpaClientValues>(
    {
        rfbpaClient,
    },
)

export function RfbpaClientProvider({children}: { children: ReactNode }) {
    const {authentication} = useAuthentication()
    const [client] = useState<AuthenticatedRfbpaClient>(rfbpaClient)

    useEffect(() => {
        client.authentication = authentication
    }, [authentication]);

    return (
        <AuthorizationContext>
            <RfbpaClientContext.Provider value={{rfbpaClient: client}}>
                {children}
            </RfbpaClientContext.Provider>
        </AuthorizationContext>
    )
}

export function useRfbpaClient() {
    return React.useContext(RfbpaClientContext)
}