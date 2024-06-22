"use client"

import {useNavigate} from "react-router"
import {useState} from "react";


export function Login() {
    const navigate = useNavigate()
    const [isLoggedIn, setIsLoggedIn] = useState<boolean>(false)

    const handleClick = () => {
        const callbackUrl = window.location.origin
        const clientId = ""
        const targetUrl = ""
    }
}
