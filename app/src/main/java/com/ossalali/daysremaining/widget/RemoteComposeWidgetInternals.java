package com.ossalali.daysremaining.widget;

import android.annotation.SuppressLint;
import androidx.compose.remote.creation.compose.widgets.WidgetInformation;
import androidx.compose.remote.creation.compose.widgets.WidgetInformationKt;
import androidx.compose.remote.creation.compose.widgets.WidgetLambdaAction;
import androidx.compose.runtime.ProvidableCompositionLocal;

/**
 * Reaches the parts of Remote Compose's widget support (1.0.0-alpha21) that are Kotlin-internal,
 * so EventWidgetReceiver can render the widget itself instead of through RCWidget, which pushes
 * views that One UI's launcher ignores. Kotlin's internal visibility doesn't exist in bytecode,
 * which is why this has to be Java. Re-check on every Remote Compose upgrade.
 */
@SuppressLint({"RestrictedApiAndroidX", "RestrictedApi"})
final class RemoteComposeWidgetInternals {
  private RemoteComposeWidgetInternals() {}

  /** Read by RemoteModifier.onClick to build each click's action id. */
  static ProvidableCompositionLocal<WidgetInformation> localWidget() {
    return WidgetInformationKt.getLocalWidget();
  }

  /** Forgets the click lambdas of the previous capture, so ids start again from zero. */
  static void clearClickLambdas() {
    WidgetLambdaAction.Companion.clear();
  }

  /** Runs the click lambda registered under [lambdaId] by the latest capture. */
  static void runClickLambda(int lambdaId) {
    WidgetLambdaAction.Companion.run(lambdaId);
  }
}
