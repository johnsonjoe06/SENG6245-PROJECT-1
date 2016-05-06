package player;


public class Player {
	
	private Piece piece;

	
	public Player(String abcContents) throws RuntimeException, NoteOutOfBoundsException {
		this.piece = Parser.parse(abcContents);
	}

	/
	public void play() throws Exception {
		System.out.println("Playing " + this.piece.getTitle() + "." );
		PieceVisitor.process(this.piece).play();
	}

}