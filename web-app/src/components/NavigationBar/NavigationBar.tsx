import {useEffect, useState} from "react";
import {styled} from "@mui/material";
import Menu from "./Menu.tsx";

export function NavigationBar() {
    const [drawerOpen] = useState(false);
    const [shadow, setShadow] = useState(false);

    useEffect(() => {
        window.addEventListener("scroll", () => {
            if (window.scrollY > 0 && !shadow) {
                setShadow(true)
            } else if (window.scrollY === 0 && shadow) {
                setShadow(false)
            }
        });
    }, [shadow])


    const StyledNavigation = styled("nav")(
        ({theme}) => ({
            position: "sticky",
            top: 0,
            left: 0,
            zIndex: 999,
            backgroundColor: theme.palette.background.default,
            display: 'flex',
            justifyContent: 'space-between',
            transition: 'box-shadow 0.2s ease-in-out',
            boxShadow: shadow && !drawerOpen ? '0 10px 15px -3px rgba(0,0,0,0.1),0 4px 6px -2px rgba(0,0,0,0.05)' : 'none',
            padding: theme.spacing(2),
            [theme.breakpoints.up('md')]: {
                padding: `${theme.spacing(2)} ${theme.spacing(4)}`,
            },
        })
    )

    return (
        <StyledNavigation id="nav">
            <Menu/>
        </StyledNavigation>
    )
}
