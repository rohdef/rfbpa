import {Box, Button, useMediaQuery} from "@mui/material";
import {OverridableStringUnion} from "@mui/types";
import {ButtonPropsVariantOverrides} from "@mui/material/Button/Button";
import {useState} from "react";
import theme from "../../styles/theme.tsx";

interface MenuItemOptions {
    href: string;
    text: string;
    variant?: OverridableStringUnion<'text' | 'outlined' | 'contained', ButtonPropsVariantOverrides>;
}

function MenuItem({href, text, variant = "text"}: MenuItemOptions) {
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
    const realm = "rfbpa"
    const client = "rfbpa"
    const handleClick = () => {
        const callbackUrl = `${window.location.origin}/`
        const targetUrl = `http://localhost:8383/realms/${realm}/protocol/openid-connect/auth?redirect_uri=${encodeURIComponent(callbackUrl)}&response_type=token&client_id=${client}&scope=openid%20email%20profile`
        window.location.href = targetUrl
    }

    return (
        <Box component="ul" sx={{
            display: "flex",
            "& li:not(:last-child)": {
                marginRight: theme.spacing(1),
            },
        }}>
            <MenuItem href="#home" text="Start"/>
            <MenuItem href="/calendar" text="Min kalender"/>
            {/*<MenuItem href="/login" text="Login" variant="outlined"/>*/}

            <Button
                component="a"
                onClick={handleClick}
                variant="outlined"
                sx={{ letterSpacing: theme.typography.standard.letterSpacing }}>
                Login
            </Button>
        </Box>
    );
}
