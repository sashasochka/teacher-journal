package ua.kpi.teacherjournal

import org.scaloid.common._
import android.app.ActionBar._
import android.content.Intent
import android.view.{View, Menu}
import android.widget.ShareActionProvider
import scala.language.postfixOps
import Journal.Sheet

class MainActivity extends SActivity { self =>
  import RandData._

  override def onCreateOptionsMenu(menu: Menu) = {
    // setup menu
    getMenuInflater.inflate(R.menu.main_activity_actions, menu)

    // setup share button
    val shareProvider = menu.findItem(R.id.action_share).getActionProvider.asInstanceOf[ShareActionProvider]
    shareProvider.setShareIntent(new Intent(Intent.ACTION_SEND).setType("text/*"))

    super.onCreateOptionsMenu(menu)
  }

  def updateTable(selectedSheet: Sheet) = {
    getFragmentManager.beginTransaction()
      .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
      .replace(R.id.table_fragment, new TableFragment(selectedSheet))
      .commit()
  }

  def setupActionBar() = {
    val actionBar = getActionBar
    val adapter = SArrayAdapter(android.R.layout.simple_spinner_dropdown_item, courses.map(_.name).toArray)
    val mNavListener = new OnNavigationListener {
      def onNavigationItemSelected(position: Int, id: Long) = {
        if (courseId != position) {
          courseId = position
          sheetId = 0
          updateTable(selectedSheet)
        }
        true
      }
    }
    actionBar.setNavigationMode(NAVIGATION_MODE_LIST)
    actionBar.setListNavigationCallbacks(adapter, mNavListener)
    actionBar.setDisplayShowTitleEnabled(false)
    actionBar.setSelectedNavigationItem(courseId)
  }

  onCreate {
    setupActionBar()
    setContentView(R.layout.main_activity)
    updateTable(selectedSheet)
    find[View](R.id.table_fragment).backgroundColor = TableFragment.backgroundColor
  }
}
