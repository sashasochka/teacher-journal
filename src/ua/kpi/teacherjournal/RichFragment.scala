package ua.kpi.teacherjournal

import android.app.Fragment
import android.os.Bundle
import org.scaloid.common._
import android.view.Surface

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

  def isLandscapeOrientation =
    List(Surface.ROTATION_0, Surface.ROTATION_180) contains windowManager.getDefaultDisplay.getRotation

  def isPortraitOrientation = !isLandscapeOrientation
}
