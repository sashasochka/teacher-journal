package ua.kpi.teacherjournal

import android.view.View
import android.widget.{HorizontalScrollView, ScrollView}
import org.scaloid.common.{SHorizontalScrollView, TraitViewGroup, SScrollView}

class SScrollViewSynchronized(view: => ScrollView, disableScrollBar: Boolean = false)
  (implicit context: android.content.Context, parentVGroup: TraitViewGroup[_] = null)
  extends SScrollView {

  setOverScrollMode(View.OVER_SCROLL_NEVER)
  verticalScrollBarEnabled = !disableScrollBar

  override def onScrollChanged(w: Int, h: Int, oldw: Int, oldh: Int) = {
    super.onScrollChanged(w, h, oldw, oldh)
    view.scrollTo(w, h)
  }
}

class SHorizontalScrollViewSynchronized(view: => HorizontalScrollView, disableScrollBar: Boolean = false)
  (implicit context: android.content.Context, parentVGroup: TraitViewGroup[_] = null)
  extends SHorizontalScrollView {
  setOverScrollMode(View.OVER_SCROLL_NEVER)

  horizontalScrollBarEnabled = !disableScrollBar

  override def onScrollChanged(w: Int, h: Int, oldw: Int, oldh: Int) = {
    super.onScrollChanged(w, h, oldw, oldh)
    view.scrollTo(w, h)
  }
}
