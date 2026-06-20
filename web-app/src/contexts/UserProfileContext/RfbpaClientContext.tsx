import React, {ReactNode, useEffect, useState} from "react";
import {RfbpaClient} from "../../api/RfbpaClient.ts"
import AuthorizationContext from "../AuthorizationContext/AuthorizationContext.tsx"
import {useAuthentication} from "../AuthenticationContext/AuthenticationContext.tsx"
import {WeekPlan} from "../../pages/Shifts/WeekPlan.ts"
import {Authentication, NoAuthentication, TokenAuthentication} from "../AuthenticationContext/Authentication.tsx"
import {Shift} from "../../pages/Shifts/Shift.ts"
import {Helper} from "../../helpers/Helper.ts"

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

    async getHelpers(): Promise<Helper[]> {
        if (this._authentication instanceof TokenAuthentication) {
            return await this.client.getHelpers(this._authentication)
        } else {
            throw new Error(`Cannot read helpers without authentication`)
        }
    }

    async getShiftsInWeek(week: string): Promise<WeekPlan> {
        if (this._authentication instanceof TokenAuthentication) {
            return await this.client.getShiftsInWeek(week, this._authentication)
        } else {
            throw new Error(`Cannot read week ${week} without authentication`)
        }
    }

    async registerIllness(shiftId: string): Promise<Shift> {
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