import React, {createContext, useEffect, useState} from "react"
import {Helper} from "../../helpers/Helper.ts"
import {RfbpaClientProvider, useRfbpaClient} from "../UserProfileContext/RfbpaClientContext.tsx"

interface HelpersContextValues {
    helpers: Helper[]
    reload: () => void
}

export const HelpersContext = createContext<HelpersContextValues>(
    {
        helpers: [],
        reload: () => {}
    }
)

export function HelpersProvider(props: {children: React.ReactNode}) {
    const {rfbpaClient} = useRfbpaClient()
    const [helpers, setHelpers] = useState<Helper[]>([])

    useEffect(() => {
        console.log("Loading helpers...")
        rfbpaClient.getHelpers()
            .then(setHelpers)
            // .catch(error => {
            //     setCalendarState(CalendarState.ERROR)
            //     setHelpers([])
            //     console.error("Error loading helpers:", error)
            // })
    }, []);

    return (
        <RfbpaClientProvider>
            <HelpersContext.Provider value={{helpers, reload: () => {}}}>
                {props.children}
            </HelpersContext.Provider>
        </RfbpaClientProvider>
    )
}

export function useHelpers() {
    return React.useContext(HelpersContext)
}