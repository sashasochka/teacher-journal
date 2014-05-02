package ua.kpi.teacherjournal

import android.app.Fragment
import android.os.Bundle

class RichFragment extends Fragment {
  implicit def ctx = getActivity

  def arg[T](argName: String) =
    getArguments.get(argName).asInstanceOf[T]

  def setArguments(args: (String, Serializable)*): RichFragment = {
    val bdl = new Bundle(args.size)
    for ((key, value) <- args)
      bdl.putSerializable(key, value)
    setArguments(bdl)
    this
  }
}
