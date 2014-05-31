package ua.kpi.teacherjournal

import android.app.{AlertDialog, Fragment, FragmentManager}
import android.content.{Context, DialogInterface}
import android.graphics.drawable.GradientDrawable
import android.graphics.Color._
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType._
import android.view._
import android.widget.{AdapterView, PopupMenu}
import org.scaloid.common._
import scala.collection.mutable.ArrayBuffer
import scala.language.postfixOps
import Journal._

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
  def marginVertical(implicit ctx: Context) = 1 dip
  def cellPaddingH(implicit ctx: Context) = 20 dip

  def cellBgColor(record: Option[Record]) = record match {
    case Some(AbsentRecord) => absentBackgroundColor
    case _ => cellColor
  }

  def update(fragmentManager: FragmentManager, selectedSheet: Sheet) = {
    val tableFragment = TableFragment(selectedSheet)
    fragmentManager.beginTransaction()
      .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
      .replace(R.id.table_fragment, tableFragment)
      .commit()
    tableFragment
  }

  def apply(sheet: Sheet) = {
    (new TableFragment).setArguments("sheet" -> sheet)
  }
}

class TableFragment extends Fragment with RichFragment {
  import TableFragment._
  import Data._

  private var headerLayout: SLinearLayout = _
  private var groupSpinner: SSpinner = _
  private var addColBtn: SImageButton = _
  private var headersScrollView: SHorizontalScrollView = _
  private var namesScrollView: SScrollViewSynchronized = _
  private var cellsHScrollView: SHorizontalScrollView = _
  private var cellsScrollView: SScrollView = _
  private val rowLayouts = ArrayBuffer[STableRow]()

  var selectedCellCoord: Option[Coord] = None
  var gradeEditCoord: Option[Coord] = None

  def addEmptyColumn() = {
    val editText = new SEditText()
      .hint(R.string.new_column)
      .textColor(BLACK)
      .singleLine(true)

    val dialog = new AlertDialog.Builder(new ContextThemeWrapper(ctx, android.R.style.Theme_Holo_Light_Dialog_NoActionBar)) {
      setTitle(R.string.enter_new_column_name)
      setView(editText)

      setPositiveButton(android.R.string.ok, {
        val newColumnName = editText.text.toString
        selectedSheet.columns += newColumnName
        for (student <- selectedSheet.students) {
          student.records += EmptyRecord
        }

        val x = headerLayout.childCount - 1
        headerLayout.addView(createHeader(x, newColumnName), x)

        for ((layout, y) <- rowLayouts.zipWithIndex) {
          layout.addView(createCell(Some(EmptyRecord), Coord(x, y)), x)
        }

        // post is used to schedule scrolling after Android adds column to its layout
        cellsHScrollView.post(cellsHScrollView.fullScroll(View.FOCUS_RIGHT))
        ()
      })

      setNegativeButton(android.R.string.cancel, () => ())
    }.show()

    editText.onTextChanged {
      dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(!editText.text.toString.isEmpty)
    }
  }

  def createHeader(index: Int, text: String) = {
    new STextView(text) {
      gravity = Gravity.CENTER_HORIZONTAL
      backgroundColor = headerColor
      textSize = 18.dip
      textColor = BLACK
      maxLines = 1
      padding(cellPaddingH, 10 dip, cellPaddingH, 10 dip)
      minimumWidth = 90.dip
      val hl = headerLayout
      <<(new hl.LayoutParams(_)).fill
        .marginRight(marginHorizontal)
        .marginBottom(marginVertical)
        .>>
      override def onMeasure(w: Int, h: Int) = {
        super.onMeasure(w, h)
        setColumnWidth(index, measuredWidth)
      }
    }
  }

  def createCell(recordOption: Option[Record], coord: Coord): STextView = {
    val row = rowLayouts(coord.y)
    val cellText = recordOption match {
      case Some(GradeRecord(grade)) =>
        if (grade == grade.toInt.toDouble) grade.toInt.toString
        else grade.toString
      case _ => ""
    }

    val cell = new STextView(cellText)
      .backgroundColor(cellBgColor(recordOption))
      .gravity(Gravity.CENTER_HORIZONTAL)
      .textSize(18 dip)
      .textColor(BLACK)
      .maxLines(1)
      .padding(cellPaddingH, 10 dip, cellPaddingH, 10 dip)
      // layout params are repeated in setColumnWidth and
      // should be changed simultaneously in case of need
      .<<(new row.LayoutParams(_))
      .marginRight(marginHorizontal)
      .marginBottom(marginVertical)
      .>>

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
          item match {
            case `gradeItem` =>
              val gradeEdit = new SEditText(cellText)
                .padding(cellPaddingH, 10 dip, cellPaddingH, 10 dip)
                .textSize(18 dip)
                .maxLines(1)
                .filters(Array[InputFilter](new InputFilter.LengthFilter(5)))
                .backgroundColor(cellColor)
                .textColor(BLACK)
                .gravity(Gravity.CENTER_VERTICAL)
                .singleLine(true)
                .inputType(TYPE_CLASS_NUMBER | TYPE_NUMBER_FLAG_DECIMAL | TYPE_NUMBER_FLAG_SIGNED)
                .<<(new row.LayoutParams(_))
                .marginRight(marginHorizontal)
                .marginBottom(marginVertical)
                .>>

              gradeEditCoord = Some(coord)

              gradeEdit.onFocusChange((v: View, hasFocus: Boolean) => {
                if (hasFocus) {
                  gradeEdit.post(inputMethodManager.showSoftInput(gradeEdit, 0))
                  gradeEdit.selectAll()
                }
              })

              gradeEdit.onKey((v: View, keyCode: Int, event: KeyEvent) => {
                if ((event.getAction == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                  endGradeEditing()
                  true
                } else false
              })

              selectedSheet.students(coord.y).records(coord.x) = EmptyRecord
              row.post(selectCell(coord))
              gradeEdit.post(gradeEdit.requestFocus())

              row.addView(gradeEdit, coord.x)
              row.removeView(cell)
            case `absentItem` => updateCellRecord(coord, AbsentRecord)
            case `presentItem` => updateCellRecord(coord, EmptyRecord)
            case `clearItem` => updateCellRecord(coord, EmptyRecord)
          }
          true
        }
      })
      popup.show()
    }

    cell.onClick {
      endGradeEditing()
      unselectCell()
      recordOption match {
        case Some(record) => selectCell(coord)
        case None =>
      }
    }

    cell.onLongClick {
      endGradeEditing()
      unselectCell()
      recordOption match {
        case Some(record) =>
          selectCell(coord)
          showPopup(record)
        case None =>
      }
      true
    }
    cell
  }

  def selectCell(coord: Coord) = {
    val d = new GradientDrawable()
    d.setStroke(2 dip, rgb(0x4d, 0x93, 0xc3))
    d.setColor(cellBgColor(Some(selectedSheet.students(coord.y).records(coord.x))))
    rowLayouts(coord.y).getChildAt(coord.x).backgroundDrawable = d
    selectedCellCoord = Some(coord)
  }

  def unselectCell() = {
    selectedCellCoord match {
      case Some(Coord(x, y)) =>
        rowLayouts(y).getChildAt(x).backgroundColor  = cellBgColor(Some(selectedSheet.students(y).records(x)))
        selectedCellCoord = None
      case None =>
    }
  }

  def updateCellRecord(cellCoord: Coord, record: Record) = {
    selectedSheet.students(cellCoord.y).records(cellCoord.x) = record
    rowLayouts(cellCoord.y).removeViewAt(cellCoord.x)
    rowLayouts(cellCoord.y).addView(createCell(Some(record), cellCoord), cellCoord.x)
  }

  def endGradeEditing() = {
    gradeEditCoord match {
      case Some(gradeCoord) =>
        val gradeEdit = rowLayouts(gradeCoord.y).getChildAt(gradeCoord.x).asInstanceOf[SEditText]
        inputMethodManager.hideSoftInputFromWindow(gradeEdit.windowToken, 0)
        val editStr = gradeEdit.text.toString
        val newRecord = if (editStr.isEmpty) EmptyRecord else GradeRecord(editStr.toDouble)
        updateCellRecord(gradeCoord, newRecord)
      case None =>
    }
    gradeEditCoord = None
  }

  def setColumnWidth(index: Int, width: Int) = {
    for ((cell, rowIndex) <- rowLayouts.map(_.getChildAt(index)).zipWithIndex) {
      val layout = rowLayouts(rowIndex)
      cell
        .<<(width, ViewGroup.LayoutParams.WRAP_CONTENT)(new layout.LayoutParams(_))
        .marginRight(marginHorizontal)
        .marginBottom(marginVertical)
        .>>
    }
  }

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle) = {
    assert(ctx != null)

    val sheet = arg[Sheet]("sheet")

    new SVerticalLayout {
      <<.fill.>>
      backgroundColor = bgColor

      this += new SLinearLayout {
        groupSpinner = SSpinner()
          .backgroundResource(R.drawable.group_spinner_bg)
          .<<.marginBottom(marginVertical).>>
          .adapter(
            SArrayAdapter(selectedCourse.sheets.map(_.name).toArray)
              .dropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
              .style(_.textColor(BLACK)
                .textSize(18 dip)
                .maxLines(1)
                .padding(cellPaddingH, 2 dip, cellPaddingH, 2 dip)
                .gravity(Gravity.CENTER_HORIZONTAL)
              ))
          .selection(sheetId)
          .onItemSelected((_: AdapterView[_], _: View, pos: Int, _: Long) => {
            if (sheetId != pos) {
              sheetId = pos
              update(getFragmentManager, selectedSheet)
            }
          })

        this += new SHorizontalScrollViewSynchronized(cellsHScrollView, disableScrollBar = true) {
          headersScrollView = this
          <<.marginLeft(marginHorizontal).>>
          this += new SLinearLayout {
            headerLayout = this
            // Headers (e.g. dates)
            for ((column, colIndex) <- sheet.columns.zipWithIndex) {
              this += createHeader(colIndex, column)
            }

            // new column adder button
            this += new SImageButton(R.drawable.add_new_col) {
              addColBtn = this
              padding(cellPaddingH, 2 dip, cellPaddingH, 2 dip)
              backgroundColor = headerColor
              <<.marginRight(marginHorizontal)
                .marginBottom(marginVertical)
                .>>
              override def onMeasure(w: Int, h: Int) = {
                super.onMeasure(w, h)
                setColumnWidth(rowLayouts.head.childCount - 1, measuredWidth)
              }

              onClick {
                addEmptyColumn()
              }
            }
          }
        }
      }

      this += new SLinearLayout {
        <<.wrap.>>
        this += new SScrollViewSynchronized(cellsScrollView, disableScrollBar = true) {
          namesScrollView = this
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
                .textColor(BLACK)
                .textSize(18 dip)
                .maxLines(1)
                .padding(cellPaddingH, 10 dip, cellPaddingH, 10 dip)
                .<<.marginBottom(marginVertical).>>
              if (student.isBoss) tv.textColor = bossColor
            }
          }
        }

        this += new SScrollViewSynchronized(namesScrollView) {
          cellsScrollView = this
          <<.marginLeft(marginHorizontal).>>
          this += new SHorizontalScrollViewSynchronized(headersScrollView) {
            cellsHScrollView = this
            this += new STableLayout {
              // Student marks (main table)
              for ((student, studentIndex) <- sheet.students.zipWithIndex) {
                val rowView = new STableRow {
                  rowLayouts += this

                  for (recordOption <- student.records.map(Some(_)) :+ None) {
                    this += createCell(recordOption, Coord(childCount, studentIndex))
                  }
                }
                this += rowView
              }
            }
          }
        }
      }
    }
  }
}
