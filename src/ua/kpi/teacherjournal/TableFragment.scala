package ua.kpi.teacherjournal

import android.app.Fragment
import android.content.Context
import android.graphics.Color._
import android.os.Bundle
import android.view.{Gravity, ViewGroup, LayoutInflater, View}
import android.widget.{AdapterView, TextView}
import org.scaloid.common._
import scala.language.postfixOps
import ua.kpi.teacherjournal.Journal._

object TableFragment {
  val headerColor = rgb(0xe7, 0xe7, 0xe7)
  val cellColor = WHITE
  val backgroundColor = rgb(0xcc, 0xcc, 0xcc)
  val absentBackgroundColor = rgb(0xFF, 0xE5, 0xE6)

  def marginRight(implicit context: Context) = 1 dip
  def marginBottom(implicit context: Context) = 1 dip
}

class TableFragment(sheet: Sheet) extends Fragment {
  import TableFragment._
  import RandData._

  def updateTable(selectedSheet: Sheet) = {
    getFragmentManager.beginTransaction()
      .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
      .replace(R.id.table_fragment, new TableFragment(selectedSheet))
      .commit()
  }

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle) = {
    implicit val ctx = getActivity
    require(ctx != null)

    new SScrollView {
      this += new SLinearLayout {
        style {
          case t: STextView => t.textColor(BLACK)
            .textSize(18 dip)
            .maxLines(1)
            .padding(20 dip, 10 dip, 20 dip, 10 dip)
        }

        this += new STableLayout {
          style {
            case t: STextView => t.backgroundColor(headerColor).<<
              .marginRight(marginRight)
              .marginBottom(marginBottom)
              .>>
            case t: SSpinner => t.backgroundColor(headerColor).<<
              .marginRight(marginRight)
              .marginBottom(marginBottom)
              .>>
          }

          this += (new SSpinner {
            adapter(SArrayAdapter(android.R.layout.simple_spinner_item, RandData.groupNames.toArray)
              .dropDownViewResource(android.R.layout.simple_spinner_dropdown_item).style(_.textColor(BLACK)
                .textSize(18 dip)
                .maxLines(1)
                .padding(20 dip, 2 dip, 20 dip, 3 dip))
              )
          }).selection(sheetId).onItemSelected((_: AdapterView[_], _: View, pos: Int, _: Long) => {
            if (sheetId != pos) {
              sheetId = pos;
              updateTable(selectedSheet)
              }
            })
          for ((student, studentIndex) <- sheet.students.zipWithIndex)
            STextView(s"${studentIndex + 1}. ${student.name}")
        }

        this += new SHorizontalScrollView {
          this += new STableLayout {

            // Header row
            this += new STableRow {

              style {
                case t: STextView => t.gravity(Gravity.CENTER_HORIZONTAL)
                  .backgroundColor(headerColor).<<
                  .marginRight(marginRight).marginBottom(marginBottom)
                  .>>
              }
              for (column <- sheet.columns)
                STextView(column)
            }

            // Student rows
            for ((student, studentIndex) <- sheet.students.zipWithIndex)
              this += new STableRow {
                style {
                  case t: STextView => t.gravity(Gravity.CENTER_HORIZONTAL)
                    .backgroundColor(cellColor).<<
                    .marginRight(marginRight)
                    .marginBottom(marginBottom)
                    .>>
                }

                for (record <- student.records) {
                  record match {
                    case GradeRecord(grade) => STextView(grade.toString)
                    case AbsentRecord => STextView().backgroundColor(absentBackgroundColor)
                    case EmptyRecord => STextView()
                  }
                }
              }
          }
        }
      }
    }
  }
}
