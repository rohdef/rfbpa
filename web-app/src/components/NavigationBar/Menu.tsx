import {Box, Button} from "@mui/material";
import {OverridableStringUnion} from "@mui/types";
import {ButtonPropsVariantOverrides} from "@mui/material/Button/Button";
import theme from "../../styles/theme.tsx";
import {useAuthentication} from "../../contexts/AuthenticationContext/AuthenticationContext.tsx";
import {Role, TokenAuthentication} from "../../contexts/AuthenticationContext/Authentication.tsx";
import {RouteObject} from "react-router-dom";
import {rfbpaRoutes} from "../../App.tsx";

interface MenuItemOptions {
    destination: RouteObject;
    text: string;
    requiredRole?: Role | null,
    variant?: OverridableStringUnion<'text' | 'outlined' | 'contained', ButtonPropsVariantOverrides>;
}

function MenuItem({destination, text, requiredRole = null, variant = "text"}: MenuItemOptions) {
    const {authentication} = useAuthentication()

    if (requiredRole && !authentication.roles().includes(requiredRole)) {
        return <></>
    }

    return (
        <li>
            <Button
                component="a"
                href={destination.path}
                variant={variant}
                sx={{letterSpacing: theme.typography.standard.letterSpacing}}>
                {text}
            </Button>
        </li>
    )
}

export default function Menu() {
    const {authentication} = useAuthentication()

    return (
        <Box component="ul" sx={{
            display: "flex",
            "& li:not(:last-child)": {
                marginRight: theme.spacing(1),
            },
        }}>
            <MenuItem destination={rfbpaRoutes.home} text="Start"/>

            <MenuItem destination={rfbpaRoutes.shifts} text="Vagter" requiredRole={Role.SHIFT_ADMIN}/>
            <MenuItem destination={rfbpaRoutes.templates} text="Skabeloner" requiredRole={Role.TEMPLATE_ADMIN}/>
            <MenuItem destination={rfbpaRoutes.calendar} text="Min kalender" requiredRole={Role.EMPLOYER_CALENDAR}/>

            {authentication instanceof TokenAuthentication ? (
                <MenuItem destination={rfbpaRoutes.logout} text="Log ud" variant="outlined"/>
            ) : (
                <MenuItem destination={rfbpaRoutes.shifts} text="Log ind" variant="outlined"/>
            )}
        </Box>
    );
}
