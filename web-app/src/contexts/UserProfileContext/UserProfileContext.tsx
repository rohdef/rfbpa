"use client"

import React, {ReactNode, useEffect, useState} from "react";
import {AuthorizationContext} from "../AuthorizationContext/AuthorizationContext";

interface UserProfileValues {
    userData?: any
}

export const UserProfileContext = React.createContext<UserProfileValues>(
    {}
)

export function UserProfileProvider({children}: { children: ReactNode }) {
    const [userData, setUserData] = useState<string>("null")

    return <AuthorizationContext>
        <UserProfileContext.Provider value={{userData}}>
            {children}
        </UserProfileContext.Provider>
    </AuthorizationContext>
}

export function useUserProfile() {
    return React.useContext(UserProfileContext)
}
