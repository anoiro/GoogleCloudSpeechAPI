// Imports the Google Cloud client library
const speech = require('@google-cloud/speech').v1p1beta1;

// Creates a client
const client = new speech.SpeechClient();

/**
 * TODO(developer): Uncomment the following line before running the sample.
 */
// const uri = path to GCS audio file e.g. `gs:/bucket/audio.wav`;

const config = {
  encoding: 'LINEAR16',
  sampleRateHertz: 44100,
  languageCode: 'ja-JP',
  enableSpeakerDiarization: true,
  diarizationSpeakerCount: 3,
  model: 'command_and_search',
};

const audio = {
  uri: 'gs://0812_0/zoom_1_test.wav',
};

const request = {
  config: config,
  audio: audio,
};

async function main() {
  const [operation] = await client.longRunningRecognize(request);
  const [response] = await operation.promise();
  // const [response] = await client.recognize(request);
  const transcription = response.results
    .map(result => result.alternatives[0].transcript)
    .join('\n');
  console.log(`Transcription: ${transcription}`);
  console.log('Speaker Diarization:');
  const result = response.results[response.results.length - 1];
  const wordsInfo = result.alternatives[0].words;
  // Note: The transcript within each result is separate and sequential per result.
  // However, the words list within an alternative includes all the words
  // from all the results thus far. Thus, to get all the words with speaker
  // tags, you only have to take the words list from the last result:
  wordsInfo.forEach(a =>
    console.log(` word: ${a.word}, speakerTag: ${a.speakerTag}`)
  );
}

main()
