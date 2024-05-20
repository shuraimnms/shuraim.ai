import torch
from torch import nn
from torchvision.utils import save_image
from transformers import BertModel, BertTokenizer

class TextEncoder(nn.Module):
    def __init__(self):
        super(TextEncoder, self).__init__()
        self.bert = BertModel.from_pretrained('bert-base-uncased')
    
    def forward(self, input_ids, attention_mask):
        outputs = self.bert(input_ids, attention_mask=attention_mask)
        return outputs.last_hidden_state[:, 0, :]

class Generator(nn.Module):
    def __init__(self):
        super(Generator, self).__init__()
        self.fc = nn.Sequential(
            nn.Linear(768, 256),
            nn.ReLU(True),
            nn.Linear(256, 512),
            nn.ReLU(True),
            nn.Linear(512, 1024),
            nn.ReLU(True),
            nn.Linear(1024, 3 * 64 * 64),
            nn.Tanh()
        )
    
    def forward(self, text_features):
        x = self.fc(text_features)
        x = x.view(-1, 3, 64, 64)
        return x

def generate_image_from_text(text):
    tokenizer = BertTokenizer.from_pretrained('bert-base-uncased')
    inputs = tokenizer(text, return_tensors='pt', truncation=True, padding='max_length', max_length=128)
    text_encoder = TextEncoder()
    generator = Generator()
    text_features = text_encoder(inputs.input_ids, inputs.attention_mask)
    with torch.no_grad():
        generated_image = generator(text_features)
    save_image(generated_image, 'generated_image.png')

if __name__ == "__main__":
    import sys
    text = sys.argv[1]
    generate_image_from_text(text)
