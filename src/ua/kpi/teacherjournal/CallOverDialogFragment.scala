package ua.kpi.teacherjournal

import android.app.{FragmentManager, Dialog, DialogFragment}
import android.graphics.Color._
import android.os.Bundle
import android.view._
import android.view.Gravity._
import org.scaloid.common._
import scala.language.postfixOps
import android.graphics.drawable.ColorDrawable

object CallOverDialogFragment {
  def show(fragmentManager: FragmentManager, names: Seq[String], answers: Seq[Boolean] = Nil) = {
    val dialog = new CallOverDialogFragment
    dialog.setArguments(
      "names" -> names.toVector,
      "answers" -> answers.toVector
    )
    dialog.show(fragmentManager, "call-over dialog")
  }
}

class CallOverDialogFragment extends DialogFragment with RichFragment {
  lazy val names = arg[Vector[String]]("names")
  lazy val answers = arg[Vector[Boolean]]("answers")

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle) = {
    assert(ctx != null)

    require(answers.size < names.size)

    new SFrameLayout {
      backgroundDrawable(R.drawable.callover_dialog)
      setStyle(DialogFragment.STYLE_NO_FRAME, android.R.style.Theme_Translucent)

      this += new SVerticalLayout {
        this += new SRelativeLayout {
          padding(5 dip, 5 dip, 5 dip, 10 dip)
          backgroundDrawable(R.drawable.callover_dialog_title)

          style {
            case s: SImageButton => s
              .backgroundColor(TRANSPARENT)
              .adjustViewBounds(true)
          }
          val backBtn = SImageButton(R.drawable.callover_prev)
            .<<(60 dip, 60 dip)
            .alignParentLeft
            .>>
            .onClick(showPrev())
          val closeBtn = SImageButton(R.drawable.callover_close)
            .onClick(dismiss())
            .<<(60 dip, 60 dip)
            .alignParentRight
            .>>
          val titleView = STextView("Перекличка за 1.03")
            .textColor(WHITE)
            .textSize(26 dip)
            .gravity(CENTER_HORIZONTAL)
            .<<
            .wrap
            .leftOf(closeBtn)
            .rightOf(backBtn)
            .centerVertical
            .>>
        }

        val nameView = STextView(names(answers.size))
          .textColor(BLACK)
          .textSize(40 dip)
          .textColor(rgb(0x54, 0x54, 0x54))
          .padding(0, 22 dip, 0, 22 dip)
          .gravity(CENTER_HORIZONTAL)
          .backgroundColor(rgb(0xfa, 0xfa, 0xfa))

        this += new SLinearLayout {
          backgroundDrawable(R.drawable.callover_dialog_bottom)
          gravity(CENTER_HORIZONTAL)

          style {
            case s: SImageButton => s
              .adjustViewBounds(true)
              .padding(20 dip, 10 dip, 20 dip, 5)
              .<<(160 dip, 80 dip)
              .marginLeft(25 dip)
              .marginBottom(10 dip)
              .>>
          }
          val absentBtn = SImageButton(R.drawable.student_absent_img)
            .backgroundDrawable(R.drawable.student_absent)
            .onClick(showNext(currentStudentIsPresent = false))
          val presentBtn = SImageButton(R.drawable.student_present_img)
            .backgroundDrawable(R.drawable.student_present)
            .onClick(showNext(currentStudentIsPresent = true))
        }
      }
    }
  }

  override def onCreateDialog(savedInstanceState: Bundle) = {
    setCancelable(false)
    val dialog = new Dialog(getActivity)
    dialog.getWindow.requestFeature(Window.FEATURE_NO_TITLE)
    dialog.getWindow.setBackgroundDrawable(new ColorDrawable(TRANSPARENT))
    dialog
  }

  def showPrev() = {
    dismiss()
    if (answers.nonEmpty) {
      CallOverDialogFragment.show(getFragmentManager, names, answers.init)
    }
  }

  def showNext(currentStudentIsPresent: Boolean) = {
    dismiss()
    if (answers.size != names.size - 1) {
      CallOverDialogFragment.show(getFragmentManager, names, answers :+ currentStudentIsPresent)
    }
  }
}
