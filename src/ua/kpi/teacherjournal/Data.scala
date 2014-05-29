package ua.kpi.teacherjournal

import android.text.format.Time
import Journal.{Course, HourMinute, TimePeriod}

object Data {
  val courses = for (courseName <- RandData.courseNames)
    yield Course(courseName, RandData.randomSheets)

  var sheetId = 0
  var courseId = 0

  def selectedCourse = courses(courseId)
  def selectedSheet = selectedCourse.sheets(sheetId)

  val classTimes = List(
    TimePeriod(HourMinute(8, 30), HourMinute(10, 5)),
    TimePeriod(HourMinute(10, 25), HourMinute(12, 0)),
    TimePeriod(HourMinute(12, 20), HourMinute(13, 55)),
    TimePeriod(HourMinute(14, 15), HourMinute(15, 50)),
    TimePeriod(HourMinute(16, 10), HourMinute(17, 45))
  )

  def timeUntilClassEnd = {
    val curTime = new Time()
    curTime.setToNow()
    val curHourMinute = HourMinute(curTime.hour, curTime.minute)
    // val curHourMinute = HourMinute(12, 59 + Random.nextInt(10)) // for debugging
    classTimes.find(c => c.start < curHourMinute && curHourMinute < c.end)
      .map(_.end - curHourMinute)
  }
}
