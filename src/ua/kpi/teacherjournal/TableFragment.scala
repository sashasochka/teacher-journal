package ua.kpi.teacherjournal

import android.app.{Fragment, FragmentManager}
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.graphics.Color._
import android.os.Bundle
import android.view._
import android.widget.AdapterView
import org.scaloid.common._
import scala.collection.mutable.ArrayBuffer
import scala.language.postfixOps
import ua.kpi.teacherjournal.Journal._

object TableFragment {
  val headerColor = rgb(0xe7, 0xe7, 0xe7)
  val cellColor = WHITE
  val bossColor = rgb(0x99, 0x32, 0x31)
  val bgColor = rgb(0xcc, 0xcc, 0xcc)
  val absentBackgroundColor = rgb(0xFF, 0xE5, 0xE6)

  def marginLeft(implicit ctx: Context) = 1 dip
  def marginBottom(implicit ctx: Context) = 1 dip

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

  var groupSpinner: SSpinner = _
  var cellsHScrollView, headersScrollView: SHorizontalScrollView = _
  var headerLayout: SLinearLayout = _
  val rowLayouts = ArrayBuffer[STableRow]()
  val cellViews = ArrayBuffer[ArrayBuffer[STextView]]()
  var addColBtn: SButton = _
  var selectedCell: Option[(STextView, Record)] = None

  def createHeader(index: Int, text: String) = {
    new STextView(text) {
      override def onMeasure(w: Int, h: Int) = {
        super.onMeasure(w, h)
        setColumnWidth(index, getMeasuredWidth)
      }
    }
  }

  def addCellToRow(record: Record, rowIndex: Int) = {
    val layout = rowLayouts(rowIndex)
    def cellBgColor(record: Record) = record match {
      case AbsentRecord => absentBackgroundColor
      case _ => cellColor
    }

    val cellText = record match {
      case GradeRecord(grade) => grade.toString
      case _ => ""
    }

    val cellView = STextView(cellText)(ctx, new layout.LayoutParams(_))
      .backgroundColor(cellBgColor(record))
      .gravity(Gravity.CENTER_HORIZONTAL)

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
  }

  def setColumnWidth(index: Int, width: Int) = {
    for ((cell, rowIndex) <- rowLayouts.map(_.getChildAt(index)).zipWithIndex) {
      val layout = rowLayouts(rowIndex)
      cell
        .<<(width, ViewGroup.LayoutParams.WRAP_CONTENT)(new layout.LayoutParams(_))
        .marginLeft(marginLeft)
        .marginBottom(marginBottom)
    }
  }

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle) = {
    assert(ctx != null)

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

      this += new SLinearLayout {
        groupSpinner = SSpinner()
          .backgroundColor(headerColor)
          .<<
          .marginLeft(marginLeft).marginBottom(marginBottom)
          .>>
          .adapter(SArrayAdapter(selectedCourse.sheets.map(_.name).toArray)
          .dropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
          .style(_.textColor(BLACK)
            .textSize(18 dip)
            .maxLines(1)
            .padding(20 dip, 5 dip, 20 dip, 4 dip)
          ))
          .selection(sheetId)
          .onItemSelected((_: AdapterView[_], _: View, pos: Int, _: Long) => {
            if (sheetId != pos) {
              sheetId = pos
              update(getFragmentManager, selectedSheet)
            }
          })

        headersScrollView = new SHorizontalScrollViewSynchronized(cellsHScrollView, disableScrollBar = true) {
          headerLayout = new SLinearLayout {
            style {
              case v: TraitTextView[_] => v
                .backgroundColor(headerColor)
                .<<.fill
                .marginLeft(marginLeft)
                .marginBottom(marginBottom)
                .>>
            }

            // Headers (e.g. dates)
            for ((column, colIndex) <- sheet.columns.zipWithIndex) {
              this += createHeader(colIndex, column)
            }

            // new column adder button
            addColBtn = new SButton("+++") {
              padding(20 dip, 0, 20 dip, 0)
              textColor = BLACK
              override def onMeasure(w: Int, h: Int) = {
                super.onMeasure(w, h)
                setColumnWidth(rowLayouts.head.getChildCount - 1, getMeasuredWidth)
              }

              onClick {
                headerLayout.removeView(addColBtn)
                headerLayout += createHeader(headerLayout.childCount, RandData.randomColumn)
                headerLayout += addColBtn

                for ((layout, rowIndex) <- rowLayouts zipWithIndex) {
                  val cols = layout.getChildCount
                  val emptyView = layout.getChildAt(cols - 1)
                  layout.removeViewAt(cols - 1)
                  addCellToRow(RandData.randomRecord, rowIndex)
                  layout += emptyView
                }
              }
            }
            this += addColBtn
          }
          this += headerLayout
        }
        this += headersScrollView
      }

      this += new SLinearLayout {
        var cellsScrollView: SScrollView = _
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
              for ((student, studentIndex) <- sheet.students.zipWithIndex) {
                val rowView = new STableRow {
                  rowLayouts += this

                  for (record <- student.records :+ EmptyRecord) {
                    addCellToRow(record, studentIndex)
                  }
                }
                this += rowView
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
