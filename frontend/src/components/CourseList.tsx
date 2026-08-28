import { useQuery } from "@tanstack/react-query"
import { fetchCourses } from "@/api/courses"

export function CourseList() {
    const { data: courses, isLoading, error } = useQuery({
        queryKey: ["courses"],
        queryFn: fetchCourses,
    })

    if (isLoading) {
        return <p>Loading courses...</p>
    }

    if (error) {
        return <p>Something went wrong: {error.message}</p>
    }

    return (
        <div className="p-6">
            <h1 className="text-2xl font-bold mb-4">Courses</h1>
            <ul className="space-y-2">
                {courses?.map((course) => (
                    <li key={course.id} className="border rounded p-4">
                        <h2 className="font-semibold">{course.title}</h2>
                        <p className="text-sm text-gray-600">{course.description}</p>
                    </li>
                ))}
            </ul>
        </div>
    )
}