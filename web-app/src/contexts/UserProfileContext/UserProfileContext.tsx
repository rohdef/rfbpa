"use client"

import React, {ReactNode, useState} from "react";
import AuthorizationContext from "../AuthorizationContext/AuthorizationContext.tsx";

interface UserProfileValues {
    userData?: any
}

export const UserProfileContext = React.createContext<UserProfileValues>(
    {}
)

export function UserProfileProvider({children}: { children: ReactNode }) {
    const [userData] = useState<string>("null")

    return <AuthorizationContext>
        <UserProfileContext.Provider value={{userData}}>
            {children}
        </UserProfileContext.Provider>
    </AuthorizationContext>
}

export function useUserProfile() {
    return React.useContext(UserProfileContext)
}
