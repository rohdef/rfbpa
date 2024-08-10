import {useAuthentication} from "../contexts/AuthenticationContext/AuthenticationContext.tsx";
import {TokenAuthentication} from "../contexts/AuthenticationContext/Authentication.tsx";
import {useNavigate} from "react-router-dom";
import {rfbpaRoutes} from "../App.tsx";
import {useEffect} from "react";

export default function Logout() {
    const {authentication, resetAuthentication} = useAuthentication()
    const navigate = useNavigate()

    useEffect(() => {
        if (authentication instanceof TokenAuthentication) {
            resetAuthentication()
        }
        navigate(rfbpaRoutes.home.path!)
    }, [authentication])

    return <h2>Logger ud</h2>
}
