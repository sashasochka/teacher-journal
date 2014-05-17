package ua.kpi.teacherjournal

import android.app.{Fragment, FragmentManager}
import android.content.Context
import android.graphics.Color._
import android.os.Bundle
import android.view._
import android.widget.AdapterView
import org.scaloid.common._
import scala.language.postfixOps
import ua.kpi.teacherjournal.Journal._
import android.graphics.drawable.GradientDrawable
import scala.collection.mutable.ArrayBuffer

object TableFragment {
  val headerColor = rgb(0xe7, 0xe7, 0xe7)
  val cellColor = WHITE
  val bossColor = rgb(0x99, 0x32, 0x31)
  val bgColor = rgb(0xcc, 0xcc, 0xcc)
  val absentBackgroundColor = rgb(0xFF, 0xE5, 0xE6)

  def marginLeft(implicit context: Context) = 1 dip
  def marginBottom(implicit context: Context) = 1 dip

  def update(fragmentManager: FragmentManager, selectedSheet: Sheet) =
    fragmentManager.beginTransaction()
      .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
      .replace(R.id.table_fragment, TableFragment(selectedSheet))
      .commit()

  def apply(sheet: Sheet) = {
    (new TableFragment).setArguments("sheet" -> sheet)
  }
}

class TableFragment extends Fragment with RichFragment {
  import TableFragment._
  import RandData._

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle) = {
    require(ctx != null)

    val sheet = arg[Sheet]("sheet")
    new SVerticalLayout {
      <<.fill
      backgroundColor = bgColor
      style {
        case t: STextView => t.textColor(BLACK)
          .textSize(18 dip)
          .maxLines(1)
          .padding(20 dip, 10 dip, 20 dip, 10 dip)
      }

      var groupSpinner: SSpinner = _
      var cellsHScrollView, headersScrollView: SHorizontalScrollView = _
      val rowLayouts = ArrayBuffer[STableRow]()
      val cellViews = ArrayBuffer[ArrayBuffer[STextView]]()

      this += new SLinearLayout {
        groupSpinner = SSpinner()
          .backgroundColor(headerColor)
          .<<.wrap
          .marginLeft(marginLeft).marginBottom(marginBottom)
          .>>
          .adapter(SArrayAdapter(selectedCourse.sheets.map(_.name).toArray)
          .dropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
          .style(_.textColor(BLACK)
          .textSize(18 dip)
          .maxLines(1)
          .padding(20 dip, 2 dip, 20 dip, 3 dip))
        ).selection(sheetId).onItemSelected((_: AdapterView[_], _: View, pos: Int, _: Long) => {
          if (sheetId != pos) {
            sheetId = pos
            update(getFragmentManager, selectedSheet)
          }
        })

        headersScrollView = new SHorizontalScrollViewSynchronized(cellsHScrollView, disableScrollBar = true) {
          this += new SLinearLayout {
            // Headers (e.g. dates)
            for ((column, colIndex) <- sheet.columns.zipWithIndex) {
              val header = new STextView(column) {
                override def onMeasure(w: Int, h: Int) = {
                  super.onMeasure(w, h)
                  for ((cell, rowIndex) <- cellViews.map(_(colIndex)).zipWithIndex) {
                    val layout = rowLayouts(rowIndex)
                    cell
                      .<<(measuredWidth, WRAP_CONTENT)(new layout.LayoutParams(_))
                      .marginLeft(marginLeft)
                      .marginBottom(marginBottom)
                  }
                }
              }.backgroundColor(headerColor)
               .<<
               .marginLeft(marginLeft)
               .marginBottom(marginBottom)
               .>>
              this += header
            }
          }
        }
        this += headersScrollView
      }

      this += new SLinearLayout {
        var cellsScrollView: SScrollView = null
        val namesScrollView = new SScrollViewSynchronized(cellsScrollView, disableScrollBar = true) {
           this += new SVerticalLayout {
            <<.wrap.>>

            override def onMeasure(w: Int, h: Int) = {
              super.onMeasure(w, h)
              groupSpinner.<<(measuredWidth, WRAP_CONTENT).>>
            }

            // student names
            for ((student, studentIndex) <- sheet.students.zipWithIndex) {
              val tv = STextView(s"${studentIndex + 1}. ${student.name}")
                .backgroundColor(headerColor)
                .<<
                .marginLeft(marginLeft)
                .marginBottom(marginBottom)
                .>>
              if (student.isBoss) tv.textColor = bossColor
            }
          }
        }

        cellsScrollView = new SScrollViewSynchronized(namesScrollView) {
          cellsHScrollView = new SHorizontalScrollViewSynchronized(headersScrollView) {
            this += new STableLayout {
              // Student marks (main table)
              var selectedCell: Option[(STextView, Record)] = None
              for ((student, studentIndex) <- sheet.students.zipWithIndex) {
                cellViews += ArrayBuffer()
                val rowView = new STableRow {
                  for (record <- student.records) {
                    style {
                      case t: STextView => t.gravity(Gravity.CENTER_HORIZONTAL)
                    }

                    def cellBgColor(record: Record) = record match {
                      case AbsentRecord => absentBackgroundColor
                      case _ => cellColor
                    }

                    val cellText = record match {
                      case GradeRecord(grade) => grade.toString
                      case _ => ""
                    }

                    val cellView = STextView(cellText)
                      .backgroundColor(cellBgColor(record))

                    cellView.onClick {
                      selectedCell match {
                        case Some((cell, rec)) => cell.backgroundColor(cellBgColor(rec))
                        case None =>
                      }
                      val d = new GradientDrawable()
                      d.setStroke(2 dip, rgb(0x4d, 0x93, 0xc3))
                      d.setColor(cellBgColor(record))
                      cellView.backgroundDrawable = d
                      selectedCell = Some(cellView, record)
                    }
                    cellViews.last += cellView
                  }
                }
                this += rowView
                rowLayouts += rowView
              }
            }
          }
          this += cellsHScrollView
        }
        this += namesScrollView
        this += cellsScrollView
      }
    }
  }
}
