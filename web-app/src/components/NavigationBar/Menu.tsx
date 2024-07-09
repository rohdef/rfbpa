import {Box, Button, useMediaQuery} from "@mui/material";
import {OverridableStringUnion} from "@mui/types";
import {ButtonPropsVariantOverrides} from "@mui/material/Button/Button";
import {useState} from "react";
import theme from "../../styles/theme.tsx";
import {useAuthentication} from "../../contexts/AuthenticationContext/AuthenticationContext.tsx";
import {Role, TokenAuthentication} from "../../contexts/AuthenticationContext/Authentication.tsx";

interface MenuItemOptions {
    href: string;
    text: string;
    requiredRole?: Role | null,
    variant?: OverridableStringUnion<'text' | 'outlined' | 'contained', ButtonPropsVariantOverrides>;
}

function MenuItem({href, text, requiredRole = null, variant = "text"}: MenuItemOptions) {
    const {authentication} = useAuthentication()
    const [drawerOpen, setDrawerOpen] = useState(false);
    // TODO probably not the best way to ask if mobile - and also, can this be styled in stead?
    const isMobile = useMediaQuery(theme.breakpoints.down('sm'));

    const toggleNavigation = () => {
        if (isMobile) {
            setDrawerOpen(!drawerOpen);

            // lock page scroll when menu is open
            // document.getElementsByTagName('body')[0].classList.toggle(classes.noScroll);
        }
    }

    if (requiredRole && !authentication.roles().includes(requiredRole)) {
        return <></>
    }

    return (
        <li>
            {isMobile ? (
                <Button component="a" href={href} variant={variant}>
                    {text}
                </Button>
            ) : (
                // TODO find a way to style this using media sizes rather than the if
                <Button
                    component="a"
                    href={href}
                    variant={variant}
                    onClick={toggleNavigation}
                    sx={{ letterSpacing: theme.typography.standard.letterSpacing }}>
                    {text}
                </Button>
            )}
        </li>
    )
}


export default function Menu() {
    const {authentication, resetAuthentication} = useAuthentication()

    return (
        <Box component="ul" sx={{
            display: "flex",
            "& li:not(:last-child)": {
                marginRight: theme.spacing(1),
            },
        }}>
            <MenuItem href="/" text="Start"/>

            <MenuItem href="/calendar" text="Min kalender"/>
            <MenuItem href="/calendar" text="Min kalender" requiredRole={Role.EMPLOYER_CALENDAR} />
            <MenuItem href="/shifts" text="Vagter" requiredRole={Role.SHIFT_ADMIN} />
            <MenuItem href="/template" text="Affyr skabelon" requiredRole={Role.TEMPLATE_ADMIN} />

            {authentication instanceof TokenAuthentication ?
                (<Button
                    component="a"
                    onClick={resetAuthentication}
                    variant="outlined"
                    sx={{ letterSpacing: theme.typography.standard.letterSpacing }}>
                    Logout
                </Button>) :
                (<> </>)}
        </Box>
    );
}
