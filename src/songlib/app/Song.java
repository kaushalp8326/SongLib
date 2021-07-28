package songlib.app;

public class Song implements Comparable<Song> {
	
	public String name;
	public String artist;
	public int year;
	public String album="";
	
	// Constructors

	public Song(String name, String artist) {
		this.name = name;
		this.artist = artist;
	}
	
	public Song(String name, String artist, int year) {
		this(name, artist);
		this.year = year;
	}
	
	public Song(String name, String artist, String album) {
		this(name, artist);
		this.album = album;
	}
	
	public Song(String name, String artist, int year, String album) {
		this(name, artist, year);
		this.album = album;
	}
	
	public String toString() {
		return name + " -- " + artist;
	}
	
	/*
	 * Case-insensitive comparison of song titles, then artists
	 */
	public int compareTo(Song other) {
		int c = this.name.toLowerCase().compareTo(other.name.toLowerCase());
		if(c == 0) {
			c = this.artist.toLowerCase().compareTo(other.artist.toLowerCase());
		}
		return c;
	}
	
	public boolean equals(Song other) {
		if(this.compareTo(other) == 0) {
			return true;
		}
		return false;
	}
	
	public boolean completelyEquals(Song other) {
		if(this.compareTo(other)==0 && this.year==other.year) {
			if(this.album==null) {
				if(other.album==null) {
					return true;
				}else {
					return false;
				}
			}else {
				if(other.album==null) {
					return false;
				}else {
					int cmp=this.album.toLowerCase().compareTo(other.album.toLowerCase());
					if(cmp==0) {
						return true;
					}else {
						return false;
					}
				}
			}
		}
		return false;
	}
}
