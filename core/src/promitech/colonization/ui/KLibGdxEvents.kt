package promitech.colonization.ui

import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.TextField
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldListener

inline public fun SimpleMessageDialog.withButton(
	titleCode: String, 
	crossinline buttonAction : (dialog : SimpleMessageDialog) -> Unit) : SimpleMessageDialog 
{
	var actionListener = object : SimpleMessageDialog.ButtonActionListener() {
		override fun buttonPressed(dialog: SimpleMessageDialog) {
			buttonAction(dialog)
		}
	}
	this.withButton(titleCode, actionListener)
	return this
}

inline public fun STable.addSelectListener( crossinline listener : (payload:Any?) -> Unit) {
	this.addSelectListener( object : STableSelectListener {
		override fun onSelect(payload: Any?) {
			listener(payload)
		}
	})
}

inline public fun STable.addSingleClickSelectListener( crossinline listener : (payload:Any?) -> Unit) {
	this.addSingleClickSelectListener( object : STableSelectListener {
		override fun onSelect(payload: Any?) {
			listener(payload)
		}
	})
}

inline public fun TextButton.addListener(
	crossinline listener : (event: ChangeListener.ChangeEvent?, actor: Actor?) -> Unit
) {

	this.addListener(object : ChangeListener() {
		override fun changed(event: ChangeListener.ChangeEvent?, actor: Actor?) = listener(event, actor)
	})
	
}

inline public fun TextField.addKeyTypedListener(
	crossinline listener : (textField: TextField?, c: Char) -> Unit 
) {
	this.setTextFieldListener(object : TextFieldListener {
		override fun keyTyped(textField: TextField?, c: Char) {
			listener(textField, c)
		}
	})
}
