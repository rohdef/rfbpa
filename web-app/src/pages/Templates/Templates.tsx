import {Box, Button, Grid, TextField} from "@mui/material";
import axios from "axios";
import {ChangeEvent, FormEvent, useState} from "react";
import {TokenAuthentication} from "../../contexts/AuthenticationContext/Authentication.tsx";
import {useAuthentication} from "../../contexts/AuthenticationContext/AuthenticationContext.tsx";
import AuthorizationContext from "../../contexts/AuthorizationContext/AuthorizationContext.tsx";

export default function Templates() {
    const {authentication} = useAuthentication()
    const [startWeek, setStartWeek] = useState("")
    const [endWeek, setEndWeek] = useState("")
    const [file, setFile] = useState<File>()
    const client = axios.create({
        baseURL: "http://localhost:8080/api/public",
    });
    function handleChange(event: ChangeEvent<HTMLInputElement>) {
        const files = event.target.files || []
        if (files[0]) {
            setFile(files[0])
        }
    }

    const handleFileUpload = (event: FormEvent<HTMLFormElement>) => {
        event.preventDefault()
        const yearWeekInterval = `${startWeek}--${endWeek}`
        if (authentication instanceof TokenAuthentication) {
            const formData = new FormData()
            if (!file) return
            formData.append('file', file)

            client.post(`/templates/${yearWeekInterval}`, formData, {
                headers: {
                    Authorization: `Bearer ${authentication.token}`
                }
            })
                .then(response => {
                    console.log(response);
                })
                .catch(error => {
                    console.error(error);
                })
        }
    }

    return (
        <AuthorizationContext>
            <div>
                <h1>Templates</h1>
                <Box component="form" autoComplete="off" onSubmit={handleFileUpload}>
                    <Grid container spacing={2}>
                        <Grid item xs={12} sm={6}>
                            <TextField
                                label="From"
                                margin="normal"
                                fullWidth
                                required
                                onChange={(e) => setStartWeek(e.target.value)}
                                variant="outlined"
                                color="secondary"
                                type="week"
                                value={startWeek}
                            />
                        </Grid>

                        <Grid item xs={12} sm={6}>
                            <TextField
                                label="To"
                                margin="normal"
                                fullWidth
                                required
                                onChange={(e) => setEndWeek(e.target.value)}
                                variant="outlined"
                                color="secondary"
                                type="week"
                                value={endWeek}
                            />
                        </Grid>
                    </Grid>

                    <TextField type="file" onChange={handleChange} inputProps={{accept: "text/yaml"}}/>
                    <Button
                        type="submit"
                        color="primary">
                        Upload
                    </Button>
                </Box>
            </div>
        </AuthorizationContext>
    )
}
