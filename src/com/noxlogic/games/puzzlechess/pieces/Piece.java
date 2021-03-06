package com.noxlogic.games.puzzlechess.pieces;

import java.util.ArrayList;

import com.noxlogic.games.puzzlechess.Board;
import com.noxlogic.games.puzzlechess.ChessPanelView;
import com.noxlogic.games.puzzlechess.games.Game;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

public abstract class Piece {
	protected int _x;						// X position
	protected int _y;						// Y position
	protected int _color;					// Piece color (BLACK | WHITE)
	protected String _tag;					// Unique tag for this piece
	protected Board _board;					// Board on which the piece resides
	protected Bitmap _bitmap = null;		// Bitmap of the piece
	protected boolean _moveable = true;		// Can piece be moved or not

	protected int _in_animation = 0;		// Piece is in animation (ie, a move)
	protected int _animation_delta_x = 0;	// Delta X for animation
	protected int _animation_delta_y = 0;	// Delta Y for animation
	protected int _animation_cur_x = 0;		// Animated X position
	protected int _animation_cur_y = 0;		// Animated Y position

	public static final int BLACK = 0;
	public static final int WHITE = 1;

	abstract protected ArrayList<int[]> _getAvailableMoves();
	abstract int getResource ();
	abstract public String getName();
	
	public Bitmap getBitmap(ChessPanelView panel) {
		if (_bitmap != null) return _bitmap;
		
		_bitmap = BitmapFactory.decodeResource(panel.getResources(), getResource());
		return _bitmap;
	}
	
	public void animatedMove (int dst_x, int dst_y, int frames) {
		_animation_cur_x = getX() * 100;
		_animation_cur_y = getY() * 100;
		
		_animation_delta_x = (dst_x - getX()) * 100 / frames;
		_animation_delta_y = (dst_y - getY()) * 100 / frames;
				
		_in_animation = frames;
		setXY(dst_x, dst_y);
	}
	
	
	public void animate () {		
		if (_in_animation <= 0) return;
				
		_animation_cur_x += _animation_delta_x;
		_animation_cur_y += _animation_delta_y;
		
		_in_animation--;
	}
	
	
	public boolean isAnimated() {
		return (_in_animation > 0);
	}
	
	protected Piece(String tag, int color, boolean moveable) {
		_tag = tag;
		_color = color;
		_moveable = moveable;
	}
	
	
	public ArrayList<int[]> getAvailableMoves () {
		return _getAvailableMoves();
	}
	
	
	protected boolean isValidAvailableField(int x, int y) {
		// X Y in range?
		if (x < 0 || x >= 8) return false;
		if (y < 0 || y >= 8) return false;
		
		// Is field enabled?
		if (! _board.isFieldEnabled(x, y)) return false;
		
		// If we cannot travel over the same cell, return
		if (_board.getGame().hasGameOption(Game.GAMEOPTION_TRACEMOVES) && _board.hasTraveled(x, y)) {
			return false;
		}
		
		// Cell free?
		Piece dst_piece = _board.getPieceFromXY(x, y);
		if (dst_piece == null) return true;
		
		// Same color in cell, 
		if (dst_piece.getColor() == getColor()) return false;
		
		// We can overtake
		if (_board.getGame().hasGameOption(Game.GAMEOPTION_CANOVERTAKE)) return true;
		
		return false;
	}
	
	public String getTag() { return _tag; }
	void setTag(String tag) { _tag = tag; }
	
	Board getBoard() { return _board; }
	public void setBoard(Board board) { _board = board; }
	
	public int getColor() { return _color; }
	void setColor(int color) { _color = color; }
	
	public int getX() {
		if (isAnimated()) return _animation_cur_x;
		return _x; 
	}
	void setX(int x) { 
		_x = x; 
	}
	
	public int getY() { 
		if (isAnimated()) return _animation_cur_y;
		return _y; 
	}
	void setY(int y) { 
		_y = y; 
	}

	int[] getXY() {
		if (isAnimated()) return new int[] { _animation_cur_x, _animation_cur_y };
		return new int[]{_x, _y}; 
	}
	
	public void setXY(int x, int y) { 
		_x = x; 
		_y = y; 
		
		if (_board != null) {
		  _board.setBorderColor(x, y, Color.BLUE);
		}
	}
	
	
	ArrayList<int[]> straightMoves(ArrayList<int[]> ret) {
		int x,y;
		
		for (y=getY()-1; y>=0; y--)  
			if (isValidAvailableField(getX(), y)) { ret.add(new int[] {getX(), y}); } else { break; }
		for (y=getY()+1; y<=7; y++)  
			if (isValidAvailableField(getX(), y)) { ret.add(new int[] {getX(), y}); } else { break; }
	
		for (x=getX()-1; x>=0; x--)  
			if (isValidAvailableField(x, getY())) { ret.add(new int[] {x, getY()}); } else { break; }
		for (x=getX()+1; x<=7; x++)  
			if (isValidAvailableField(x, getY())) { ret.add(new int[] {x, getY()}); } else { break; }
		
		return ret;
	}
	
	ArrayList<int[]> diagonalMoves(ArrayList<int[]> ret) {
		int y,i,j;
		
		for (i=-1,y=getY()-1; y>=0; y--,i--)
			if (isValidAvailableField(getX()-i, y)) { ret.add(new int[] {getX()-i, y}); } else { break; }
		for (j=1,y=getY()-1; y>=0; y--,j++)
			if (isValidAvailableField(getX()-j, y)) { ret.add(new int[] {getX()-j, y}); } else { break; }

		for (i=-1,y=getY()+1; y<=7; y++,i--) 
			if (isValidAvailableField(getX()-i, y)) { ret.add(new int[] {getX()-i, y}); } else { break; }
		for (j=1,y=getY()+1; y<=7; y++,j++)
			if (isValidAvailableField(getX()-j, y)) { ret.add(new int[] {getX()-j, y}); } else { break; }

		return ret;
	}
	
	ArrayList<int[]> knightMoves(ArrayList<int[]> ret) {
		int x,y;
		
		x = getX()+1; 
		y = getY()-2;
		if (isValidAvailableField(x, y)) ret.add(new int[] {x, y});

		x = getX()+2; 
		y = getY()-1;
		if (isValidAvailableField(x, y)) ret.add(new int[] {x, y});

		x = getX()+2; 
		y = getY()+1;
		if (isValidAvailableField(x, y)) ret.add(new int[] {x, y});

		x = getX()+1; 
		y = getY()+2;
		if (isValidAvailableField(x, y)) ret.add(new int[] {x, y});

		x = getX()-1; 
		y = getY()+2;
		if (isValidAvailableField(x, y)) ret.add(new int[] {x, y});

		x = getX()-2; 
		y = getY()+1;
		if (isValidAvailableField(x, y)) ret.add(new int[] {x, y});

		x = getX()-2; 
		y = getY()-1;
		if (isValidAvailableField(x, y)) ret.add(new int[] {x, y});

		x = getX()-1; 
		y = getY()-2;
		if (isValidAvailableField(x, y)) ret.add(new int[] {x, y});
		
		return ret;
	}
	

	
	ArrayList<int[]> singleUpDownMoves(ArrayList<int[]> ret) {
		int x,y;
		
		x = getX(); 
		y = getY()-1;
		if (isValidAvailableField(x, y)) ret.add(new int[] {x, y});
	
		x = getX(); 
		y = getY()+1;
		if (isValidAvailableField(x, y)) ret.add(new int[] {x, y});
		
		return ret;
	}
	
	ArrayList<int[]> singleLeftRightMoves(ArrayList<int[]> ret) {
		int x,y;
	
		x = getX()-1; 
		y = getY();
		if (isValidAvailableField(x, y)) ret.add(new int[] {x, y});

		x = getX()+1; 
		y = getY();
		if (isValidAvailableField(x, y)) ret.add(new int[] {x, y});

		return ret;
	}
	
	ArrayList<int[]> singleDiagonalMoves(ArrayList<int[]> ret) {
		int x,y;
		
		x = getX()-1; 
		y = getY()-1;
		if (isValidAvailableField(x, y)) ret.add(new int[] {x, y});
		
		x = getX()+1; 
		y = getY()-1;
		if (isValidAvailableField(x, y)) ret.add(new int[] {x, y});
		
		x = getX()-1; 
		y = getY()+1;
		if (isValidAvailableField(x, y)) ret.add(new int[] {x, y});

		x = getX()+1; 
		y = getY()+1;
		if (isValidAvailableField(x, y)) ret.add(new int[] {x, y});

		return ret;
	}

	/**
	 * Mark the piece as moveable or not. When not moveable, a user cannot
	 * select this piece to be moved.
	 * 
	 * @param moveable
	 */
	public void setMoveable(boolean moveable) {
		_moveable = moveable;
	}
	
	/**
	 * Returns wether or not the piece is moveable or not
	 * 
	 * @return boolean True when moveable, false otherwise
	 */
	public boolean isMoveable() {
		return _moveable;
	}

	
}
