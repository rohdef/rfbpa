import {Button, LinearProgress, TextField} from "@mui/material";
import axios from "axios";
import {ChangeEvent, FormEvent, useState} from "react";

export default function Templates() {
    const [uploadProgress, setUploadProgress] = useState(0)
    const [file, setFile] = useState<File>()
    const client = axios.create({
        baseURL: "http://localhost:8080/",
    });
    function handleChange(event: ChangeEvent<HTMLInputElement>) {
        const files = event.target.files || []
        if (files[0]) {
            setFile(files[0])
        }
    }

    const handleFileUpload = (event: FormEvent<HTMLFormElement>) => {
        event.preventDefault()
        const formData = new FormData()
        if (!file) return
        formData.append('file', file)

        client.post('/templates', formData, {
            onUploadProgress: progressEvent => {
                const percentCompleted = Math.round((progressEvent.loaded * 100) / progressEvent.total)
                setUploadProgress(percentCompleted)
            },
        })
            .then(response => {
                console.log(response);
            })
            .catch(error => {
                console.error(error);
            })
    }

    return (
        <div>
            <h1>Templates</h1>
            <LinearProgress variant="determinate" value={uploadProgress} />

            <form onSubmit={handleFileUpload}>
                <TextField type="file" onChange={handleChange} inputProps={{accept:"text/yaml"}} />
                <Button
                    type="submit"
                    color="primary">
                    Upload
                </Button>
            </form>
        </div>
    )
}
