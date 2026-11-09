package github.paroj.dsub2000.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import github.paroj.dsub2000.adapter.EntryGridAdapter;
import github.paroj.dsub2000.adapter.PodcastChannelAdapter;
import github.paroj.dsub2000.adapter.SearchAdapter;
import github.paroj.dsub2000.adapter.SectionAdapter;

public class ListDividerDecoration extends RecyclerView.ItemDecoration {
	private final Drawable divider;
	private final int height;
	private final Rect bounds = new Rect();

	public ListDividerDecoration(Context context) {
		TypedArray attributes = context.obtainStyledAttributes(new int[] {android.R.attr.listDivider});
		divider = attributes.getDrawable(0);
		attributes.recycle();
		if(divider == null) {
			throw new IllegalStateException("The list theme must define android:listDivider");
		}
		height = Math.max(1, Math.round(context.getResources().getDisplayMetrics().density));
	}

	private boolean shouldDrawDivider(RecyclerView parent, int position) {
		RecyclerView.Adapter<?> adapter = parent.getAdapter();
		if(adapter == null || position == RecyclerView.NO_POSITION || position >= adapter.getItemCount() - 1) {
			return false;
		}
		int viewType = adapter.getItemViewType(position);
		if(adapter instanceof SectionAdapter && viewType == SectionAdapter.VIEW_TYPE_HEADER) {
			return false;
		}
		if(parent.getLayoutManager() instanceof GridLayoutManager) {
			if(((adapter instanceof EntryGridAdapter || adapter instanceof SearchAdapter)
					&& viewType == EntryGridAdapter.VIEW_TYPE_ALBUM_CELL)
					|| (adapter instanceof PodcastChannelAdapter
					&& viewType == PodcastChannelAdapter.VIEW_TYPE_PODCAST_CELL)) {
				return false;
			}
			GridLayoutManager layoutManager = (GridLayoutManager) parent.getLayoutManager();
			return layoutManager.getSpanSizeLookup().getSpanSize(position) == layoutManager.getSpanCount();
		}
		return true;
	}

	@Override
	public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
		outRect.set(0, 0, 0, shouldDrawDivider(parent, parent.getChildAdapterPosition(view)) ? height : 0);
	}

	@Override
	public void onDraw(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
		int saved = canvas.save();
		if(parent.getClipToPadding()) {
			canvas.clipRect(parent.getPaddingLeft(), parent.getPaddingTop(),
					parent.getWidth() - parent.getPaddingRight(), parent.getHeight() - parent.getPaddingBottom());
		}
		for(int i = 0; i < parent.getChildCount(); i++) {
			View child = parent.getChildAt(i);
			if(!shouldDrawDivider(parent, parent.getChildAdapterPosition(child))) {
				continue;
			}
			parent.getDecoratedBoundsWithMargins(child, bounds);
			int bottom = bounds.bottom + Math.round(child.getTranslationY());
			divider.setBounds(parent.getPaddingLeft(), bottom - height,
					parent.getWidth() - parent.getPaddingRight(), bottom);
			divider.draw(canvas);
		}
		canvas.restoreToCount(saved);
	}
}
