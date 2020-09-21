// Imports the Google Cloud client library
import com.google.cloud.speech.v1.RecognitionAudio;
import com.google.cloud.speech.v1.RecognitionConfig;
import com.google.cloud.speech.v1.RecognitionConfig.AudioEncoding;
import com.google.cloud.speech.v1.RecognizeResponse;
import com.google.cloud.speech.v1.SpeechClient;
import com.google.cloud.speech.v1.SpeechRecognitionAlternative;
import com.google.cloud.speech.v1.SpeechRecognitionResult;
import com.google.protobuf.ByteString;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

class Diarization {

  public static void main(String[] args) {
    // System.out.println("Hello, world.");
    // System.out.println(args[0]);
    System.out.println(args[0]);
    transcribeDiarizationGcs(args[0]);
  }

  /**
   * Transcribe a remote audio file using speaker diarization.
   *
   * @param gcsUri the path to an audio file.
   */
  public static void transcribeDiarizationGcs(String gcsUri) throws Exception {
    try (SpeechClient speechClient = SpeechClient.create()) {
      SpeakerDiarizationConfig speakerDiarizationConfig = SpeakerDiarizationConfig
        .newBuilder()
        .setEnableSpeakerDiarization(true)
        .setMinSpeakerCount(2)
        .setMaxSpeakerCount(3)
        .build();

      // Configure request to enable Speaker diarization
      RecognitionConfig config = RecognitionConfig
        .newBuilder()
        .setEncoding(AudioEncoding.LINEAR16)
        .setLanguageCode("ja-JP")
        .setSampleRateHertz(44100)
        .setDiarizationConfig(speakerDiarizationConfig)
        .build();

      // Set the remote path for the audio file
      RecognitionAudio audio = RecognitionAudio
        .newBuilder()
        .setUri(gcsUri)
        .build();

      // Use non-blocking call for getting file transcription
      OperationFuture<LongRunningRecognizeResponse, LongRunningRecognizeMetadata> response = speechClient.longRunningRecognizeAsync(
        config,
        audio
      );

      while (!response.isDone()) {
        System.out.println("Waiting for response...");
        Thread.sleep(10000);
      }

      // Speaker Tags are only included in the last result object, which has only one alternative.
      LongRunningRecognizeResponse longRunningRecognizeResponse = response.get();
      SpeechRecognitionAlternative alternative = longRunningRecognizeResponse
        .getResults(longRunningRecognizeResponse.getResultsCount() - 1)
        .getAlternatives(0);

      // The alternative is made up of WordInfo objects that contain the speaker_tag.
      WordInfo wordInfo = alternative.getWords(0);
      int currentSpeakerTag = wordInfo.getSpeakerTag();

      // For each word, get all the words associated with one speaker, once the speaker changes,
      // add a new line with the new speaker and their spoken words.
      StringBuilder speakerWords = new StringBuilder(
        String.format(
          "Speaker %d: %s",
          wordInfo.getSpeakerTag(),
          wordInfo.getWord()
        )
      );

      for (int i = 1; i < alternative.getWordsCount(); i++) {
        wordInfo = alternative.getWords(i);
        if (currentSpeakerTag == wordInfo.getSpeakerTag()) {
          speakerWords.append(" ");
          speakerWords.append(wordInfo.getWord());
        } else {
          speakerWords.append(
            String.format(
              "\nSpeaker %d: %s",
              wordInfo.getSpeakerTag(),
              wordInfo.getWord()
            )
          );
          currentSpeakerTag = wordInfo.getSpeakerTag();
        }
      }

      System.out.println(speakerWords.toString());
    }
  }
}

