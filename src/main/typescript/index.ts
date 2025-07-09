import axios from 'axios';

// --- 1. 取得するデータの型定義 ---
// APIから返されるユーザーデータの構造を定義します
interface ChatMessage {
  id: number;
  messageId: string;
  quoteId: string;
  name: string;
  message: string;
  attachmentUrl: string;
  createDate : string;
}

// --- 2. DOM要素の取得 ---
// データを表示するコンテナ要素を取得します
const chatMessageContainer = document.getElementById('chatMessageContainer') as HTMLElement;

// --- 3. Axiosで全件データ取得とDOM操作を行う非同期関数 ---
async function fetchAndDisplayChatMessage(): Promise<void> {
  try {
    // Axiosを使って全ユーザーデータを取得します
    // レスポンスのデータ部分の型を `User[]` (Userオブジェクトの配列) として指定します
    const response = await axios.get<ChatMessage[]>('/chat');
    const chatMessages: ChatMessage[] = response.data; // 取得したデータ

    // 読み込み中のメッセージをクリアします
    chatMessageContainer.innerHTML = '';

    // 取得したユーザーデータを元にDOMを操作します
    chatMessages.forEach(chatMessage => {
      // --- 4. DOM操作: ユーザーごとにカード要素を作成 ---
      const messageDiv = document.createElement('div');
      messageDiv.className = 'message-div'; // CSSクラスを適用

      const head = document.createElement('p');
      head.textContent = `id: ${chatMessage.messageId} name:${chatMessage.name}`;

	  const main = document.createElement('p');
	  const img = document.createElement('img') as HTMLImageElement;
	  
	  let mainContents : string = '';
	  if (chatMessage.quoteId != null) {
		mainContents += `>> ${chatMessage.quoteId}`;
	  }
	  mainContents += `${chatMessage.message}`;
	  if (chatMessage.attachmentUrl != null) {
		img.src = chatMessage.attachmentUrl;
	  }
	  main.textContent = mainContents;
	  main.appendChild(img);
	  
      // 作成した要素をユーザーカードに追加
      messageDiv.appendChild(head);
      messageDiv.appendChild(main);

      // ユーザーカードをコンテナに追加
      chatMessageContainer.appendChild(messageDiv);
    });

  } catch (error) {
    // エラーハンドリング
    console.error('データの取得または表示中にエラーが発生しました:', error);
    chatMessageContainer.innerHTML = '<p>データの読み込みに失敗しました。</p>';

    // Axiosのエラーの場合、詳細情報を出力
    if (axios.isAxiosError(error) && error.response) {
      console.error('APIレスポンスエラー:', error.response.status, error.response.data);
    }
  }
}

// アプリケーション起動時にデータを取得して表示する関数を呼び出します
fetchAndDisplayChatMessage();