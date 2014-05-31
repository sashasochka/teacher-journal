package ua.kpi.teacherjournal

import android.app.ActionBar._
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.view.{Menu, MenuItem}
import au.com.bytecode.opencsv._
import org.scaloid.common._
import java.io.{FileWriter, File}
import resource._
import scala.language.postfixOps
import Journal.{AbsentRecord, EmptyRecord}
import TableFragment.Coord

class MainActivity extends SActivity { self =>
  import Data._

  private var tableFragment: TableFragment = _

  override def onCreateOptionsMenu(menu: Menu) = {
    getMenuInflater.inflate(R.menu.main_activity_actions, menu)
    super.onCreateOptionsMenu(menu)
  }

  def setupActionBar() = {
    val actionBar = getActionBar
    val adapter = SArrayAdapter(android.R.layout.simple_spinner_dropdown_item, courses.map(_.name).toArray)
    val mNavListener = new OnNavigationListener {
      def onNavigationItemSelected(position: Int, id: Long) = {
        if (courseId != position) {
          courseId = position
          sheetId = 0
          tableFragment = TableFragment.update(getFragmentManager, selectedSheet)
        }
        true
      }
    }
    actionBar.setNavigationMode(NAVIGATION_MODE_LIST)
    actionBar.setListNavigationCallbacks(adapter, mNavListener)
    actionBar.setDisplayShowTitleEnabled(false)
    actionBar.setSelectedNavigationItem(courseId)
  }

  /**
   * Share table data as csv file using email application
   * @param item Not used but required argument
   */
  def shareData(item: MenuItem = null) = {
    val filename = "share_data.csv"
    val csvFile = new File(s"${Environment.getExternalStorageDirectory}${File.separator}$filename")
    csvFile.createNewFile()
    for (fileWriter <- managed(new FileWriter(csvFile))) {
      for (writer <- managed(new CSVWriter(fileWriter))) {
        writer.writeNext((selectedSheet.name +: selectedSheet.columns).toArray)
        for (student <- selectedSheet.students) {
          writer.writeNext((student.name +: student.records.map(_.csvRepr)).toArray)
        }
      }
    }
    if (!csvFile.exists || !csvFile.canRead) {
      toast("Attachment Error")
    } else {
      val receiver = accountManager.getAccountsByType("com.google").headOption.map(_.name).orNull
      val intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", receiver, null))
        .putExtra(Intent.EXTRA_SUBJECT, s"Sheet: ${selectedSheet.name}")
        .putExtra(Intent.EXTRA_STREAM, Uri.fromFile(csvFile))
      startActivity(Intent.createChooser(intent, "Send email..."))
    }
  }

  /**
   * @param item Not used but required argument
   */
  def startCallOver(item: MenuItem = null) = {
    tableFragment.selectedCellCoord match {
      case Some(Coord(x, _)) =>
        CallOverDialogFragment.show(getFragmentManager, selectedSheet.columns(x),
          selectedSheet.students.map(_.name)) onFinish { answers =>
          for ((answer, rowIndex) <- answers.zipWithIndex) {
            val oldRecord = selectedSheet.students(rowIndex).records(x)
            val newRecord = if (answer) EmptyRecord else AbsentRecord
            if (oldRecord != newRecord && (oldRecord == AbsentRecord || !answer))
              tableFragment.updateCellRecord(Coord(x = x, y = rowIndex), newRecord)
          }
        }
      case None => toast(R.string.choose_column_first)
    }
  }

  /**
   * Exit activity
   * @param item Not used but required argument
   */
  def exit(item: MenuItem = null) = {
    finish()
  }

  onCreate {
    setupActionBar()
    setContentView(R.layout.main_activity)
    tableFragment = TableFragment.update(getFragmentManager, selectedSheet)
    BottomBar.setup(getFragmentManager)
  }
}
