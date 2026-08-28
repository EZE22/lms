import type { Course } from "@/types/course"

const BASE_URL = "http://localhost:8080"

export async function fetchCourses(): Promise<Course[]> {
    const response = await fetch(`${BASE_URL}/api/courses`)
    if (!response.ok) {
        throw new Error("Failed to fetch courses")
    }
    return response.json()
}