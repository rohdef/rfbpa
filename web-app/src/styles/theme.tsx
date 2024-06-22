"use client"

import {createTheme, responsiveFontSizes} from "@mui/material";

import {background, common, error, grey, other, primary, secondary, success, text, warning} from './colors';

declare module '@mui/material/styles' {
    interface TypographyVariants {
        standard: React.CSSProperties
        stylized: React.CSSProperties
    }

    // allow configuration using `createTheme`
    interface TypographyVariantsOptions {
        standard?: React.CSSProperties
        stylized?: React.CSSProperties
    }
}

declare module '@mui/material/Typography' {
    interface TypographyPropsVariantOverrides {
        standard: true
        stylized: true
    }
}

const theme = createTheme({
    palette: {
        common,
        text,
        primary,
        secondary,
        error,
        warning,
        success,
        grey,
        background,
        divider: other.divider,
    },
    typography: {
        standard: {
            fontFamily: "Roboto, Helvetica, Arial, sans-serif",
            letterSpacing: "0.02857em",
        },
        stylized: {
            fontSize: "5rem",
            fontFamily: "Pacifico, cursive",
            letterSpacing: "-0.01562em",
        },
        h2: {
            fontSize: "3.3rem",
        },
        fontFamily: "Roboto, Helvetica, Arial, sans-serif",

    },
    components: {
        MuiButton: {
            styleOverrides: {
                text: {
                    letterSpacing: "0.00938em",
                },
                contained: {
                    letterSpacing: "0.02857em",
                },
                outlined: {
                    letterSpacing: "0.02857em",
                }
            },
        },
        MuiTypography: {
            styleOverrides: {
                root: {
                    letterSpacing: "0.00735em",
                },
                body1: {
                    letterSpacing: "0.00938em",
                },
                h2: {
                    letterSpacing: "-0.00833em",
                },
                h3: {
                    letterSpacing: 0,
                },
                h5: {
                    letterSpacing: 0,
                },
                subtitle1: {
                    letterSpacing: "0.00938em",
                },
            }
        }
    }
});

export default responsiveFontSizes(theme, { breakpoints: ['xs', 'sm', 'md', 'lg'] })
