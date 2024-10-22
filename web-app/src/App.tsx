import './App.css'
import {CssBaseline, ThemeProvider} from "@mui/material";
import {NavigationBar} from "./components/NavigationBar/NavigationBar.tsx";
import {AuthenticationProvider} from "./contexts/AuthenticationContext/AuthenticationContext.tsx";
import theme from "./styles/theme.tsx";
import {createBrowserRouter, RouteObject, RouterProvider} from "react-router-dom";
import Welcome from "./pages/Welcome/Welcome.tsx";
import Calendar from "./pages/Calendar/Calendar.tsx";
import Shifts from "./pages/Shifts/Shifts.tsx";
import Templates from "./pages/Templates/Templates.tsx";
import Logout from "./pages/Logout.tsx";

interface RfbpaRoutes {
    calendar: RouteObject,
    home: RouteObject,
    logout: RouteObject,
    shifts: RouteObject,
    templates: RouteObject,
}

export const rfbpaRoutes: RfbpaRoutes = {
    calendar: {
        path: "/calendar",
        element: <Calendar/>,
    },
    home: {
        path: "/",
        element: <Welcome/>,
    },
    logout: {
        path: "/logout",
        element: <Logout/>,
    },
    shifts: {
        path: "/shifts",
        element: <Shifts/>,
    },
    templates: {
        path: "/templates",
        element: <Templates/>,
    },
}

function App() {
    const routes = createBrowserRouter(Object.values(rfbpaRoutes))

    return (
        <ThemeProvider theme={theme}>
            <CssBaseline/>
            <AuthenticationProvider>
                <NavigationBar/>
                <RouterProvider router={routes}/>
            </AuthenticationProvider>
        </ThemeProvider>
    )
}

export default App
