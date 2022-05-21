package br.upe.simulations.JLT18.Channel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class GraphPlotter {
	public static void plot(String graphName) throws IOException {
		// StringBuffer content = new StringBuffer();
		URL url = new URL("http://127.0.0.1:5000/" + graphName);

		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");

		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine = in.readLine();

		System.out.println(inputLine);
	}
}
