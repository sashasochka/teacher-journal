package ua.kpi.teacherjournal

import android.app.{Fragment, FragmentManager}
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.graphics.Color._
import android.os.Bundle
import android.view._
import android.widget.{PopupMenu, AdapterView}
import org.scaloid.common._
import scala.collection.mutable.ArrayBuffer
import scala.language.postfixOps
import ua.kpi.teacherjournal.Journal._
import scala.util.Random

object TableFragment {
  val headerColor = rgb(0xe7, 0xe7, 0xe7)
  val cellColor = WHITE
  val bossColor = rgb(0x99, 0x32, 0x31)
  val bgColor = rgb(0xcc, 0xcc, 0xcc)
  val absentBackgroundColor = rgb(0xFF, 0xE5, 0xE6)

  case class Coord(x: Int, y: Int) {
    require(x >= 0)
    require(y >= 0)
  }

  def marginHorizontal(implicit ctx: Context) = 1 dip
  def marginBottom(implicit ctx: Context) = 1 dip
  def cellPaddingH(implicit ctx: Context) = 20 dip

  def cellBgColor(record: Option[Record]) = record match {
    case Some(AbsentRecord) => absentBackgroundColor
    case _ => cellColor
  }

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
  var addColBtn: SImageButton = _
  var selectedCell: Option[(STextView, Record)] = None

  def createHeader(index: Int, text: String) = {
    new STextView(text) {
      gravity = Gravity.CENTER_HORIZONTAL
      minimumWidth = 80 dip
      override def onMeasure(w: Int, h: Int) = {
        super.onMeasure(w, h)
        setColumnWidth(index, getMeasuredWidth)
      }
    }
  }

  def unselectCell() = {
    selectedCell match {
      case Some((cell, rec)) =>
        cell.backgroundColor(cellBgColor(Some(rec)))
        selectedCell = None
      case None =>
    }
  }

  def createCell(recordOption: Option[Record], coord: Coord): STextView = {
    val row = rowLayouts(coord.y)
    val cellText = recordOption match {
      case Some(GradeRecord(grade)) => grade.toString
      case _ => ""
    }

    val cell = new STextView(cellText)
      .backgroundColor(cellBgColor(recordOption))
      .gravity(Gravity.CENTER_HORIZONTAL)
      .textSize(18 dip)
      .textColor(BLACK)
      .maxLines(1)
      .padding(cellPaddingH, 10 dip, cellPaddingH, 10 dip)
      .<<(new row.LayoutParams(_))
      .marginRight(marginHorizontal)
      .marginBottom(marginBottom)
      .>>

    def select(record: Record) = {
      val d = new GradientDrawable()
      d.setStroke(2 dip, rgb(0x4d, 0x93, 0xc3))
      d.setColor(cellBgColor(recordOption))
      cell.backgroundDrawable = d
      selectedCell = Some((cell, record))
    }

    def showPopup(record: Record) {
      val popup = new PopupMenu(ctx, cell)
      popup.getMenuInflater.inflate(R.menu.cell_actions, popup.getMenu)
      val menu = popup.getMenu
      val gradeItem = menu.findItem(R.id.grade_menu_item)
      val absentItem = menu.findItem(R.id.absent_menu_item).setVisible(record != AbsentRecord)
      val presentItem = menu.findItem(R.id.present_menu_item).setVisible(record == AbsentRecord)
      val clearItem = menu.findItem(R.id.clear_menu_item).setVisible(record != EmptyRecord && record != AbsentRecord)

      popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener {
        def onMenuItemClick(item: MenuItem) = {
          val newRecord = item match {
            case `gradeItem` => Some(GradeRecord(Random.nextInt(10)))
            case `absentItem` => Some(AbsentRecord)
            case `presentItem` => Some(EmptyRecord)
            case `clearItem` => Some(EmptyRecord)
          }
          row.addView(createCell(newRecord, coord), coord.x)
          row.removeView(cell)
          true
        }
      })
      popup.show()
    }

    cell.onClick {
      unselectCell()
      recordOption match {
        case Some(record) => select(record)
        case None =>
      }
    }

    cell.onLongClick {
      unselectCell()
      recordOption match {
        case Some(record) =>
          select(record)
          showPopup(record)
        case None =>
      }
      true
    }
    cell
  }

  def setColumnWidth(index: Int, width: Int) = {
    for ((cell, rowIndex) <- rowLayouts.map(_.getChildAt(index)).zipWithIndex) {
      val layout = rowLayouts(rowIndex)
      cell
        .<<(width, ViewGroup.LayoutParams.WRAP_CONTENT)(new layout.LayoutParams(_))
        .marginRight(marginHorizontal)
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
          .padding(cellPaddingH, 10 dip, cellPaddingH, 10 dip)
      }

      this += new SLinearLayout {
        groupSpinner = SSpinner()
          .backgroundResource(R.drawable.group_spinner_bg)
          .<<
          .marginBottom(marginBottom)
          .>>
          .adapter(
            SArrayAdapter(selectedCourse.sheets.map(_.name).toArray)
              .dropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
              .style(_.textColor(BLACK)
                .textSize(18 dip)
                .maxLines(1)
                .padding(cellPaddingH, 2 dip, cellPaddingH, 3 dip)
                .gravity(Gravity.CENTER_HORIZONTAL)
              ))
          .selection(sheetId)
          .onItemSelected((_: AdapterView[_], _: View, pos: Int, _: Long) => {
            if (sheetId != pos) {
              sheetId = pos
              update(getFragmentManager, selectedSheet)
            }
          })

        headersScrollView = new SHorizontalScrollViewSynchronized(cellsHScrollView, disableScrollBar = true) {
          <<.marginLeft(marginHorizontal).>>
          headerLayout = new SLinearLayout {
            style {
              case v: TraitView[_] => v
                .backgroundColor(headerColor)
                .<<.fill
                .marginRight(marginHorizontal)
                .marginBottom(marginBottom)
                .>>
            }

            // Headers (e.g. dates)
            for ((column, colIndex) <- sheet.columns.zipWithIndex) {
              this += createHeader(colIndex, column)
            }

            // new column adder button
            addColBtn = new SImageButton(R.drawable.add_new_col) {
              padding(cellPaddingH, 2 dip, cellPaddingH, 2 dip)

              override def onMeasure(w: Int, h: Int) = {
                super.onMeasure(w, h)
                setColumnWidth(rowLayouts.head.getChildCount - 1, getMeasuredWidth)
              }

              onClick {
                headerLayout.removeView(addColBtn)
                headerLayout += createHeader(headerLayout.childCount, RandData.randomColumnName)
                headerLayout += addColBtn

                for ((layout, y) <- rowLayouts.zipWithIndex) {
                  val cols = layout.getChildCount
                  val emptyView = layout.getChildAt(cols - 1)
                  layout.removeViewAt(cols - 1)
                  layout += createCell(Some(EmptyRecord), Coord(cols, y))
                  layout += emptyView
                }

                // post is used to schedule scrolling after Android adds column to its layout
                cellsHScrollView.post(cellsHScrollView.fullScroll(View.FOCUS_RIGHT))
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
                .<<.marginBottom(marginBottom).>>
              if (student.isBoss) tv.textColor = bossColor
            }
          }
        }

        cellsScrollView = new SScrollViewSynchronized(namesScrollView) {
          <<.marginLeft(marginHorizontal).>>
          cellsHScrollView = new SHorizontalScrollViewSynchronized(headersScrollView) {
            this += new STableLayout {
              // Student marks (main table)
              for ((student, studentIndex) <- sheet.students.zipWithIndex) {
                val rowView = new STableRow {
                  rowLayouts += this

                  for (recordOption <- student.records.map(Some(_)) :+ None) {
                    this += createCell(recordOption, Coord(getChildCount, studentIndex))
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
