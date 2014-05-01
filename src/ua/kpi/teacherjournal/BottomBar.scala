package ua.kpi.teacherjournal

import android.app.Fragment
import android.content.Context
import android.graphics.Color._
import android.os.Bundle
import android.view.{ViewGroup, LayoutInflater}
import org.scaloid.common._
import scala.language.postfixOps

object BottomBar extends Fragment {
  val backgroundColor = rgb(0x1D, 0x2E, 0x35)

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle) = {
    implicit val ctx: Context = getActivity
    new SRelativeLayout {
      val lastSync = STextView("Остання синхронізація").textSize(12 dip)
    }
  }
}
