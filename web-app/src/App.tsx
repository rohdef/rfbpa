import './App.css'
import {CssBaseline, ThemeProvider} from "@mui/material";
import {NavigationBar} from "./components/NavigationBar/NavigationBar.tsx";
import {AuthenticationProvider} from "./contexts/AuthenticationContext/AuthenticationContext.tsx";
import theme from "./styles/theme.tsx";
import {createBrowserRouter, RouterProvider} from "react-router-dom";
import Welcome from "./pages/Welcome/Welcome.tsx";
import Calendar from "./pages/Calendar/Calendar.tsx";

function App() {
    const routes = createBrowserRouter([
        {
            path: "/",
            element: <Welcome />,
        },
        {
            path: "/Calendar",
            element: <Calendar />,
        },
    ])

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
