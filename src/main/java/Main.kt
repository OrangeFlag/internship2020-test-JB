import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import kotlin.math.*


data class Point(val latitude: Float, val longitude: Float) {
    private val r = 6360; // geocentric radius in SPB

    fun distance(end: Point): Float {
        val dLat = (end.latitude - this.latitude).toRadians();
        val dLon = (end.longitude - this.longitude).toRadians();
        val a = sin(dLat / 2).pow(2) +
                cos(this.latitude.toRadians()) * cos(end.latitude.toRadians()) * sin(dLon / 2).pow(2)
        return (2 * r * asin(sqrt(a))).toFloat(); // distance in km
    }
}

fun Float.toRadians(): Double {
    return this * (Math.PI / 180)
}

data class Participants(val passengers: Collection<Person>, val drivers: Collection<Person>)
data class Person(val id: UUID, val finishPoint: Point)

fun main() {
    val (passengers, drivers) = readPoints()
    for (passenger in passengers) {
        val suggestedDrivers = suggestDrivers(passenger, drivers)
        println("Passenger point: ${passenger.finishPoint.latitude}, ${passenger.finishPoint.longitude}")
        for (driver in suggestedDrivers) {
            println("  ${driver.finishPoint.latitude}, ${driver.finishPoint.longitude}")
        }
    }
}

fun suggestDrivers(passenger: Person, drivers: Collection<Person>): Collection<Person> {
    return drivers.sortedWith(compareBy { passenger.finishPoint.distance(it.finishPoint) })
}

private fun readPoints(): Participants {
    val pathToResource = Paths.get(Point::class.java.getResource("latlons").toURI())
    val allPoints = Files.readAllLines(pathToResource).map { asPoint(it) }.shuffled()
    val passengers = allPoints.slice(0..9).map { Person(UUID.randomUUID(), it) }
    val drivers = allPoints.slice(10..19).map { Person(UUID.randomUUID(), it) }
    return Participants(passengers, drivers)
}

private fun asPoint(it: String): Point {
    val (lat, lon) = it.split(", ")
    return Point(lat.toFloat(), lon.toFloat())
}
