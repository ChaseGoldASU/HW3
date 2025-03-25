package databaseHelperFunctions;

//Compression imports
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

//Decompression imports
import java.io.ByteArrayInputStream;
import java.util.zip.GZIPInputStream;

/*
 * I was worried that if too many questions and answers were added then it would make too much data in the database and cause problems.
 * Thus I plan to make 2 databases, one for questions and one for answers, and to compress the texts then storing the strings
 */

//Currently unused, but avalible for use
public class StringCompressor {

	// Compress a string using GZIP
	public static byte[] compress(String qa) throws IOException {
		ByteArrayOutputStream byteS = new ByteArrayOutputStream();
		try (GZIPOutputStream gzipS = new GZIPOutputStream(byteS)) {
			gzipS.write(qa.getBytes());
			gzipS.finish();
		}
		return byteS.toByteArray();
	}

	// Decompress the Base64 encoded string back to the origional sting
	public static String decompress(byte[] compressedQA) throws IOException {
		try (GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(compressedQA))) {
			return new String(gzip.readAllBytes(), "UTF-8");
		}
	}

	public static void main(String[] args) throws IOException {
		StringCompressor compressor = new StringCompressor();
		String original = "This is a test string that will be compressed and decompressed.";

		byte[] compressed = compressor.compress(original);
		System.out.println("Compressed: " + compressed);
		System.out.println("Compressed Size: " + compressed.length + " bytes");

		String decompressed = compressor.decompress(compressed);
		System.out.println("Decompressed: " + decompressed);

		System.out.println("Match: " + original.equals(decompressed));
	}
}