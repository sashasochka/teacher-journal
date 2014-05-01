package ua.kpi.teacherjournal

import org.scaloid.common._
import android.app.ActionBar._
import android.content.Intent
import android.view.{View, Menu}
import android.widget.ShareActionProvider
import scala.language.postfixOps

class MainActivity extends SActivity { self =>

  override def onCreateOptionsMenu(menu: Menu) = {
    // setup menu
    getMenuInflater.inflate(R.menu.main_activity_actions, menu)

    // setup share button
    val shareProvider = menu.findItem(R.id.action_share).getActionProvider.asInstanceOf[ShareActionProvider]
    shareProvider.setShareIntent(new Intent(Intent.ACTION_SEND).setType("text/*"))

    super.onCreateOptionsMenu(menu)
  }

  onCreate {

    import RandData._

    val actionBar = getActionBar
    val adapter = SArrayAdapter(android.R.layout.simple_spinner_dropdown_item, courses.map(_.name).toArray)
    val mNavListener = new OnNavigationListener {
      def onNavigationItemSelected(position: Int, id: Long) = {
        if (courseId != position) {
          courseId = position
          sheetId = 0
          getFragmentManager.beginTransaction()
            .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
            .replace(R.id.table_fragment, new TableFragment(selectedGroup))
            .commit()
        }
        true
      }
    }
    actionBar.setNavigationMode(NAVIGATION_MODE_LIST)
    actionBar.setListNavigationCallbacks(adapter, mNavListener)
    actionBar.setDisplayShowTitleEnabled(false)
    actionBar.setSelectedNavigationItem(courseId)

    setContentView(R.layout.main_activity)

    getFragmentManager.beginTransaction()
      .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
      .add(R.id.table_fragment, new TableFragment(selectedGroup))
      .commit()
    find[View](R.id.table_fragment).backgroundColor = TableFragment.backgroundColor
  }
}
