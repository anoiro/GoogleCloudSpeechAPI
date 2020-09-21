// Imports the Google Cloud client library
const speech = require('@google-cloud/speech').v1p1beta1;

// Creates a client
const client = new speech.SpeechClient();

/**
 * TODO(developer): Uncomment the following lines before running the sample.
 */
// const gcsUri = 'gs://my-bucket/audio.raw';
// const encoding = 'Encoding of the audio file, e.g. LINEAR16';
// const sampleRateHertz = 16000;
// const languageCode = 'BCP-47 language code, e.g. en-US';

const config = {
  encoding: 'LINEAR16',
  sampleRateHertz: 44100,
  languageCode: 'ja-JP',
};

const audio = {
  uri: 'gs://0812_0/zoom_1_test.wav',
};

const request = {
  config: config,
  audio: audio,
};

// // Detects speech in the audio file. This creates a recognition job that you
// // can wait for now, or get its result later.
// const [operation] = await client.longRunningRecognize(request);
// // Get a Promise representation of the final result of the job
// const [response] = await operation.promise();
// const transcription = response.results
//   .map(result => result.alternatives[0].transcript)
//   .join('\n');
// console.log(`Transcription: ${transcription}`);

// earlier code here

async function main() {
  const [operation] = await client.longRunningRecognize(request);
  const [response] = await operation.promise();
  const transcription = response.results
    .map(result => result.alternatives[0].transcript)
    .join('\n');
  console.log(`Transcription: ${transcription}`);
}

main()
